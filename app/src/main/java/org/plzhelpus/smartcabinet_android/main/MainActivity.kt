package org.plzhelpus.smartcabinet_android.main

import android.content.Context
import android.content.Intent
import android.os.AsyncTask
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.google.firebase.auth.FirebaseAuth
import android.text.TextUtils
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseUser
import android.support.design.widget.Snackbar
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.firestore.*
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import kotlinx.android.synthetic.main.dialog_add_cabinet.view.*
import kotlinx.android.synthetic.main.dialog_add_member.view.*
import kotlinx.android.synthetic.main.dialog_create_group.view.*
import kotlinx.android.synthetic.main.dialog_edit_cabinet.view.*
import kotlinx.android.synthetic.main.nav_drawer.*
import kotlinx.android.synthetic.main.nav_header_main.*
import org.plzhelpus.smartcabinet_android.*
import org.plzhelpus.smartcabinet_android.R
import org.plzhelpus.smartcabinet_android.groupInfo.GroupSettingActivity
import org.plzhelpus.smartcabinet_android.auth.AuthUiActivity
import org.plzhelpus.smartcabinet_android.groupInfo.GroupInfoFragmentPagerAdapter
import org.plzhelpus.smartcabinet_android.groupInfo.admin.AdminListItemHandler
import org.plzhelpus.smartcabinet_android.groupInfo.cabinet.CabinetListItemHandler
import org.plzhelpus.smartcabinet_android.groupInfo.member.MemberListItemHandler
import java.util.*
import kotlin.collections.HashMap


/**
 * Created by Donghwan Kim on 2018-03-23.
 *
 * 앱의 메인 화면 액티비티
 */

class MainActivity : AppCompatActivity(),
        FirebaseAuth.AuthStateListener,
        GroupListItemHandler<DocumentSnapshot>,
        AdminListItemHandler<DocumentSnapshot>,
        CabinetListItemHandler<DocumentSnapshot>,
        MemberListItemHandler<DocumentSnapshot> {
    private var mGroupListListenerRegistration : ListenerRegistration? = null
    private var mCurrentGroupListenerRegistration : ListenerRegistration? = null
    private var mCurrentGroup : DocumentReference? = null
    private lateinit var mAuth : FirebaseAuth
    private lateinit var mDb : FirebaseFirestore
    private lateinit var mFunctions : FirebaseFunctions

    companion object {
        private val TAG = "MainActivity"
        // 액티비티가 실행되자 마자 정보를 보여줘야 되는 그룹
        private val FIRST_SEEN_GROUP= "FIRST_SEEN_GROUP"

        fun createIntent(context: Context, firstSeenGroupName: String? = null): Intent {
            return Intent()
                    .putExtra(FIRST_SEEN_GROUP, firstSeenGroupName)
                    .setClass(context, MainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)
        mAuth = FirebaseAuth.getInstance()
        mDb = FirebaseFirestore.getInstance()
        mFunctions = FirebaseFunctions.getInstance()

        // 네비게이션 드로어 설정
        ActionBarDrawerToggle(
                this,
                drawer_layout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close).apply {
            drawer_layout.addDrawerListener(this)
        }.syncState()

        // 그룹 정보에 뷰페이저 적용
        group_tablayout.setupWithViewPager(group_pager)
        group_pager.adapter = GroupInfoFragmentPagerAdapter(null, this, supportFragmentManager)

        // 유저가 로그인했는지 확인
        mAuth.currentUser?.let { currentUser ->
            updateUserInfoUI(currentUser)
            // 그룹 목록 적용
            group_list.run{
                layoutManager = LinearLayoutManager(this@MainActivity)
                adapter = GroupRecyclerViewAdapter(ArrayList(), this@MainActivity)
                setHasFixedSize(true)
            }
            registerGroupList(currentUser, intent.getStringExtra(FIRST_SEEN_GROUP))
        } ?: run { switchToSignInUI() }

        // 네비게이션 드로어에 그룹 추가 버튼 구현
        create_new_group_button.setOnClickListener{
            createNewGroup()
            drawer_layout.closeDrawers()
        }

        // 네비게이션 드로어에 사용자 로그아웃 버튼 구현
        user_sign_out_button.setOnClickListener {
            // AuthUI가 로그아웃하는 중에 리스너를 트리거할 수 있기 때문에 미리 해제함.
            mGroupListListenerRegistration?.remove()
            mCurrentGroupListenerRegistration?.remove()
            mAuth.removeAuthStateListener(this)
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            switchToSignInUI()
                        } else {
                            Log.w(TAG, "signOut:failed", task.exception)
                            showSnackbar(R.string.sign_out_failed)
                            mAuth.addAuthStateListener(this)
                        }
                    }
        }
    }

    /**
     * 새 그룹을 생성함
     */
    private fun createNewGroup() {
        Log.d(TAG, "Create group button clicked")
        val createGroupDialog = layoutInflater.inflate(R.layout.dialog_create_group, null)
        AlertDialog.Builder(this)
                .setTitle(R.string.create_group_dialog_title)
                .setView(createGroupDialog)
                .setPositiveButton(R.string.create_group_positive_button, {
                    dialog, id ->
                    if (TextUtils.isEmpty(createGroupDialog?.create_group_group_name_input?.text)){
                        Log.w(TAG, "Create group failed")
                        showSnackbar(R.string.create_group_failed)
                        return@setPositiveButton
                    }
                    val data : MutableMap<String, Any?> = HashMap()
                    data.put("groupName", createGroupDialog?.create_group_group_name_input?.text.toString())
                    mFunctions.getHttpsCallable("createGroup")
                            .call(data)
                            .continueWith { task ->
                                task.result.data
                            }
                            .addOnSuccessListener {
                                Log.d(TAG, "Create group successfully")
                                showSnackbar(R.string.create_group_successfully)
                                // TODO 만약 기회가 된다면 새로 생성된 그룹으로 변경해줘야 함.
                            }
                            .addOnFailureListener {exception ->
                                Log.w(TAG, "Create group failed", exception)
                                showSnackbar(R.string.create_group_failed)
                            }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, {
                    dialog, id ->
                }).show()

    }


    /**
     * 속한 그룹이 없는 유저일 때를 처리하는 메서드
     */
    private fun handleNoGroups() {
        startActivity(NoGroupActivity.createIntent(this))
        finish()
    }

    /**
     * 로그인 화면으로 전환함.
     */
    private fun switchToSignInUI() {
        startActivity(AuthUiActivity.createIntent(this))
        finish()
    }

    /**
     * 보여주고 있는 그룹을 변경함.
     */
    private fun changeGroupInfo(groupListItemDocumentSnapshot : DocumentSnapshot) {
        groupListItemDocumentSnapshot.getDocumentReference(GROUP_REF)?.let { newCurrentGroup ->
            Log.d(TAG, "Changing group now")
            Log.d(TAG, "document snapshot - ${groupListItemDocumentSnapshot.data}")
            (group_pager?.adapter as GroupInfoFragmentPagerAdapter?)?.updateGroupInfo(newCurrentGroup)
            mCurrentGroup = newCurrentGroup
            registerCurrentGroup()
        }
    }

    /**
     * 유저가 로그인 했다면 네비게이션 드로어의 프로필 칸을 채움.
     */
    private fun updateUserInfoUI(user : FirebaseUser?) {
        if(TextUtils.isEmpty(user?.email)){
            user_email.setText(R.string.no_email)
        } else {
            user_email?.apply{
                this.text = user?.email // TextUtils.isEmpty가 user.email의 null 확인도 함.
            }
        }
    }

    /**
     * 현재 보고 있는 그룹에 대한 정보의 변경사항을 계속 받아오게 함.
     */
    private fun registerCurrentGroup() {
        // 해당 뷰가 존재하지 않으면 실행하면 안됨.
        if(group_info_owner_email == null ||
                group_info_group_name == null) return
        mCurrentGroup?.let{ currentGroup ->
            // 다른 그룹에 대한 변경사항 리스너 등록을 해제해야 함.
            mCurrentGroupListenerRegistration?.remove()
            // 그룹 문서의 변경사항을 받게 함.
            mCurrentGroupListenerRegistration = currentGroup.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let { exception ->
                    Log.w(TAG, "Current group - Listen failed.", exception)
                    return@addSnapshotListener
                }

                // 문서스냅샷이 엉뚱한 곳을 가리키지 않았을 경우,
                if (documentSnapshot != null && documentSnapshot.exists()){
                    Log.d(TAG, "Current group - data found")
                    group_info_group_name?.apply{
                        this.text = documentSnapshot.getString(GROUP_NAME)
                    }
                    group_info_owner_email?.apply{
                        this.text = documentSnapshot.getString(OWNER_EMAIL)
                    }
                } else {
                    Log.d(TAG, "Current group data: null")
                }
            }
        }
    }

    /**
     * 그룹 목록의 변경을 듣는 리스너를 등록함
     */
    private fun registerGroupList(user: FirebaseUser, firstSeenGroupName: String? = null) {
        // 해당 뷰가 존재하지 않으면 실행하면 안됨.
        if(group_list == null) return
        // 기존에 있다면 지워야 함.
        mGroupListListenerRegistration?.remove()
        // 그룹 목록이 Firestore의 변경 사항을 받게 등록함.
        mDb.collection(USERS).document(user.uid).collection(PARTICIPATED_GROUP).run{
            mGroupListListenerRegistration = addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let{ exception ->
                    Log.w(TAG, "Participated group - Listen failed.", exception)
                    handleNoGroups()
                    return@addSnapshotListener
                }

                querySnapshot?.let { participatedGroupsSnapshot ->
                    Log.d(TAG, "Participated group - data found")
                    (group_list?.adapter as GroupRecyclerViewAdapter?)?.updateList(participatedGroupsSnapshot.documents)
                    if (participatedGroupsSnapshot.isEmpty) {
                        handleNoGroups()
                    } else {
                        // 어느 그룹을 보여줄 지 정해줘야 함.
                        object : AsyncTask<DocumentSnapshot, Unit, DocumentSnapshot>() {
                            override fun doInBackground(vararg param: DocumentSnapshot): DocumentSnapshot {
                                // 만약 화면으로 보고 있던 그룹이 있다면
                                mCurrentGroup?.let{ currentGroup ->
                                    // 만약 보고 있던 그룹이 삭제되었다면, 그룹 목록의 첫 번째 그룹으로 변경함
                                    if (param.none { document -> currentGroup.id == document.id }) {
                                        Log.d(TAG, "Group that you was watching is not visible now")
                                        return param[0]
                                    }
                                } ?: let{
                                    // 만약 처음 보여주어야 할 그룹이 정해져 있다면 해당 그룹으로 변경함
                                    firstSeenGroupName?.let{ firstSeenGroupName ->
                                        // 만약 해당 그룹이 그룹 목록에 존재하면 그 그룹으로 변경함
                                        param.find { document ->
                                            document.getString(GROUP_NAME) == firstSeenGroupName
                                        }?.let{ findResult ->
                                            return findResult
                                        }
                                    }
                                }
                                // 어떠한 조건에도 해당하지 않으면 그룹 목록의 첫 번째 그룹으로 변경함
                                return param[0]
                            }

                            override fun onPostExecute(result: DocumentSnapshot) {
                                super.onPostExecute(result)
                                changeGroupInfo(result)
                            }
                        }.execute(*(participatedGroupsSnapshot.documents.toTypedArray()))
                    }
                }?. run {
                    Log.d(TAG, "Participated group - data null")
                }
            }
        }
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        // 주의: UI쓰레드에서 작동함.
        // 현재는 단일 firebaseAuth로 동작하므로 굳이 어떤 firebaseAuth 인스턴스가 상태가 바뀌었는지
        // 확인할 필요가 없음.
        firebaseAuth.currentUser?.let{ currentUser ->
            registerGroupList(currentUser)
            updateUserInfoUI(currentUser)
        } ?: run {
            Log.w(TAG, "Auth has changed")
            switchToSignInUI()
        }
    }

    override fun onStart() {
        super.onStart()
        // 유저가 로그인 했는지 확인
        mAuth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        mGroupListListenerRegistration?.remove()
        mCurrentGroupListenerRegistration?.remove()
        mAuth.removeAuthStateListener(this)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_cabinet -> {
                mCurrentGroup?.let{ currentGroup ->
                    val addCabinetDialog = layoutInflater.inflate(R.layout.dialog_add_cabinet, null)
                    AlertDialog.Builder(this)
                            .setTitle(R.string.add_cabinet_dialog_title)
                            .setView(addCabinetDialog)
                            .setPositiveButton(R.string.add_cabinet_positive_button, {
                                dialog, id ->
                                if(TextUtils.isEmpty(addCabinetDialog?.add_cabinet_id_input?.text) ||
                                        TextUtils.isEmpty(addCabinetDialog?.add_cabinet_key_input?.text))
                                {
                                    Log.w(TAG, "Add cabinet failed")
                                    showSnackbar(R.string.add_cabinet_failed)
                                    return@setPositiveButton
                                }
                                val data : MutableMap<String, Any?> = HashMap()
                                data.put("groupId", currentGroup.id)
                                data.put("cabinetId", addCabinetDialog?.add_cabinet_id_input?.text.toString())
                                data.put("serialKey", addCabinetDialog?.add_cabinet_key_input?.text.toString())
                                mFunctions.getHttpsCallable("addCabinetInGroup")
                                        .call(data)
                                        .continueWith { task ->
                                            task.result.data
                                        }
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Add cabinet successfully")
                                            showSnackbar(R.string.add_cabinet_successfully)
                                        }
                                        .addOnFailureListener {exception ->
                                            Log.w(TAG, "Add cabinet failed", exception)
                                            showSnackbar(R.string.add_cabinet_failed)
                                        }
                            })
                            .setNegativeButton(R.string.alert_dialog_cancel, {
                                dialog, id ->
                            }).show()
                }
                return true
            }
            R.id.action_add_member -> {
                mCurrentGroup?.let{ currentGroup ->
                    val addMemberDialog = layoutInflater.inflate(R.layout.dialog_add_member, null)
                    val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                    builder.setTitle(R.string.add_member_dialog_title)
                            .setView(addMemberDialog)
                            .setPositiveButton(R.string.add_member_positive_button, {
                                dialog, id ->
                                if(TextUtils.isEmpty(addMemberDialog?.add_member_email_input?.text) ||
                                        (addMemberDialog?.add_member_email_input?.text.toString() == mAuth.currentUser?.email)){
                                    Log.w(TAG, "Add member failed")
                                    showSnackbar(R.string.add_member_failed)
                                    return@setPositiveButton
                                }
                                val data : MutableMap<String, Any?> = HashMap()
                                data.put("groupId", currentGroup.id)
                                data.put("email", addMemberDialog?.add_member_email_input?.text.toString())
                                mFunctions.getHttpsCallable("addMemberInGroup")
                                        .call(data)
                                        .continueWith { task ->
                                            task.result.data
                                        }
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Add member successfully")
                                            showSnackbar(R.string.add_member_successfully)
                                        }
                                        .addOnFailureListener {exception ->
                                            Log.w(TAG, "Add member failed", exception)
                                            showSnackbar(R.string.add_member_failed)
                                        }
                            })
                            .setNegativeButton(R.string.alert_dialog_cancel, {
                                dialog, id ->
                            }).show()
                }
                return true
            }
            R.id.action_settings -> {
                mCurrentGroup?.let{startActivity(GroupSettingActivity.createIntent(this, it.id))}
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onGroupListItemClicked(item: DocumentSnapshot) {
        changeGroupInfo(item)
        drawer_layout.closeDrawers()
    }

    override fun demoteAdminToMember(item: DocumentSnapshot) {
        // 자기 자신의 권한을 낮출 수 없음.
        if(item.id == mAuth.currentUser?.uid){
            Log.w(TAG, "demote admin to member failed - try to demote myself")
            showSnackbar(R.string.demote_to_member_failed)
            return
        }
        item.reference.parent.parent?.collection(MEMBER_REF)?.let{ memberRef ->
            mDb.runTransaction { transaction ->
                val data: MutableMap<String, Any?> = HashMap()
                data.put(EMAIL, item.getString(EMAIL))
                data.put(USER_REF, item.getDocumentReference(USER_REF))
                transaction.set(memberRef.document(item.id), data)
                transaction.delete(item.reference)
                return@runTransaction
            }.addOnSuccessListener {
                Log.d(TAG, "demote admin to member successfully")
                showSnackbar(R.string.demote_to_member_successfully)
            }.addOnFailureListener { exception ->
                Log.w(TAG, "demote admin to member failed", exception)
                showSnackbar(R.string.demote_to_member_failed)
            }
        }
    }

    override fun delegateOwnershipToAdmin(item: DocumentSnapshot) {
        item.reference.parent.parent?.let{ groupRef ->
            AlertDialog.Builder(this)
                    .setTitle(R.string.delegate_ownership_dialog_title)
                    .setPositiveButton(R.string.delegate_ownership_positive_button, { dialog, id ->
                        if(item.id == mAuth.currentUser?.uid){
                            Log.w(TAG, "delegate ownership to admin failed")
                            showSnackbar(R.string.delegate_ownership_failed)
                            return@setPositiveButton
                        }
                        mDb.runTransaction{ transaction ->
                            val groupDocument = transaction.get(groupRef)
                            val oldOwnerData : MutableMap<String, Any?> = HashMap()
                            val ownerRef = groupDocument.getDocumentReference(OWNER_REF) ?: throw FirebaseFirestoreException("owner_ref is null", FirebaseFirestoreException.Code.ABORTED)
                            oldOwnerData.put(EMAIL, groupDocument.getString(OWNER_EMAIL))
                            oldOwnerData.put(USER_REF, ownerRef)
                            val newOwnerData : MutableMap<String, Any?> = HashMap()
                            newOwnerData.put(OWNER_EMAIL, item.getString(EMAIL))
                            newOwnerData.put(OWNER_REF, item.getDocumentReference(USER_REF))
                            transaction.set(groupRef.collection(ADMIN_REF).document(ownerRef.id), oldOwnerData)
                            transaction.delete(item.reference)
                            transaction.update(groupRef, newOwnerData)
                            return@runTransaction
                        }.addOnSuccessListener{
                            Log.d(TAG, "delegate ownership to admin successfully")
                            showSnackbar(R.string.delegate_ownership_successfully)
                        }.addOnFailureListener{ exception ->
                            Log.w(TAG, "delegate ownership to admin failed", exception)
                            showSnackbar(R.string.delegate_ownership_failed)
                        }
                    })
                    .setNegativeButton(R.string.alert_dialog_cancel, { dialog, id ->
                    }).show()
        }
    }

    override fun deleteAdmin(item: DocumentSnapshot) {
        item.reference.parent.parent?.let{ groupRef ->
            AlertDialog.Builder(this)
                    .setTitle(R.string.delete_admin_dialog_title)
                    .setPositiveButton(R.string.delete_admin_positive_button, {
                        dialog, id ->
                        // 자기 자신을 삭제할 수 없음
                        if(item.id == mAuth.currentUser?.uid){
                            Log.w(TAG, "Delete admin failed - try to delete myself")
                            showSnackbar(R.string.delete_admin_failed)
                            return@setPositiveButton
                        }
                        mDb.runTransaction {transaction ->
                            transaction.delete(mDb.collection(USERS).document(item.id).collection(PARTICIPATED_GROUP).document(groupRef.id))
                            transaction.delete(item.reference)
                            return@runTransaction
                        }.addOnSuccessListener {
                            Log.d(TAG, "Delete admin successfully")
                            showSnackbar(R.string.delete_admin_successfully)
                        }.addOnFailureListener { exception ->
                            Log.w(TAG, "Delete admin failed", exception)
                            showSnackbar(R.string.delete_admin_failed)
                        }
                    })
                    .setNegativeButton(R.string.alert_dialog_cancel, {
                        dialog, id ->
                    }).show()
        }
    }

    override fun openOrCloseCabinet(item: DocumentSnapshot) {
        val data : MutableMap<String, Any?> = HashMap()
        data.put("cabinetId", item.id)
        mFunctions.getHttpsCallable("openOrCloseCabinet")
                .call(data)
                .continueWith { task ->
                    task.result.data
                }
                .addOnSuccessListener {
                    Log.d(TAG, "Open/close cabinet successfully")
                    showSnackbar(R.string.open_close_cabinet_successfully)
                }
                .addOnFailureListener { exception ->
                    Log.w(TAG, "Open/Close cabinet failed", exception)
                    showSnackbar(R.string.open_close_cabinet_failed)
                }
    }

    override fun deleteCabinet(item: DocumentSnapshot) {
        AlertDialog.Builder(this)
                .setTitle(R.string.delete_cabinet_dialog_title)
                .setPositiveButton(R.string.delete_cabinet_positive_button, {
                    dialog, id ->
                    item.reference.delete()
                            .addOnSuccessListener {
                                Log.d(TAG, "Delete cabinet successfully")
                                showSnackbar(R.string.delete_cabinet_successfully)
                            }
                            .addOnFailureListener {exception ->
                                Log.w(TAG, "Delete cabinet failed", exception)
                                showSnackbar(R.string.delete_cabinet_failed)
                            }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, {
                    dialog, id ->
                }).show()
    }

    override fun editCabinetDescription(item: DocumentSnapshot) {
        val editCabinetDialog = layoutInflater.inflate(R.layout.dialog_edit_cabinet, null)
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        builder.setTitle(R.string.edit_cabinet_dialog_title)
                .setView(editCabinetDialog)
                .setPositiveButton(R.string.edit_cabinet_positive_button, {
                    dialog, id ->
                    val data : MutableMap<String, Any?> = HashMap()
                    data.put(DESCRIPTION, editCabinetDialog?.edit_cabinet_description_input?.text.toString())
                    item.reference.update(data)
                            .addOnSuccessListener {
                                Log.d(TAG, "Edit cabinet description successfully")
                                showSnackbar(R.string.edit_cabinet_successfully)
                            }
                            .addOnFailureListener {exception ->
                                Log.w(TAG, "Edit cabinet description failed", exception)
                                showSnackbar(R.string.edit_cabinet_failed)
                            }
                })
                .setNegativeButton(R.string.alert_dialog_cancel, {
                    dialog, id ->
                }).show()
    }

    override fun promoteMemberToAdmin(item: DocumentSnapshot) {
        // 자기 자신의 권한을 높힐 수 없음.
        if(item.id == mAuth.currentUser?.uid){
            Log.w(TAG, "promote member to admin failed - try to promote myself")
            showSnackbar(R.string.promote_to_admin_failed)
            return
        }
        item.reference.parent.parent?.let{ groupRef ->
            mDb.runTransaction{ transaction ->
                val adminRef = groupRef.collection(ADMIN_REF)
                val data : MutableMap<String, Any?> = HashMap()
                data.put(EMAIL, item.getString(EMAIL))
                data.put(USER_REF, item.getDocumentReference(USER_REF))
                transaction.set(adminRef.document(item.id), data)
                transaction.delete(item.reference)
                return@runTransaction
            }.addOnSuccessListener{
                Log.d(TAG, "promote member to admin successfully")
                showSnackbar(R.string.promote_to_admin_successfully)

            }.addOnFailureListener{ exception ->
                Log.w(TAG, "promote member to admin failed", exception)
                showSnackbar(R.string.promote_to_admin_failed)
            }
        }
    }

    override fun delegateOwnershipToMember(item: DocumentSnapshot) {
        item.reference.parent.parent?.let{ groupRef ->
            AlertDialog.Builder(this)
                    .setTitle(R.string.delegate_ownership_dialog_title)
                    .setPositiveButton(R.string.delegate_ownership_positive_button, { dialog, id ->
                        if(item.id == mAuth.currentUser?.uid){
                            Log.w(TAG, "delegate ownership to member failed")
                            showSnackbar(R.string.delegate_ownership_failed)
                            return@setPositiveButton
                        }
                        mDb.runTransaction{ transaction ->
                            val groupDocument = transaction.get(groupRef)
                            val oldOwnerData : MutableMap<String, Any?> = HashMap()
                            val ownerRef = groupDocument.getDocumentReference(OWNER_REF) ?: throw FirebaseFirestoreException("owner_ref is null", FirebaseFirestoreException.Code.ABORTED)
                            oldOwnerData.put(EMAIL, groupDocument.getString(OWNER_EMAIL))
                            oldOwnerData.put(USER_REF, ownerRef)
                            val newOwnerData : MutableMap<String, Any?> = HashMap()
                            newOwnerData.put(OWNER_EMAIL, item.getString(EMAIL))
                            newOwnerData.put(OWNER_REF, item.getDocumentReference(USER_REF))
                            transaction.set(groupRef.collection(ADMIN_REF).document(ownerRef.id), oldOwnerData)
                            transaction.delete(item.reference)
                            transaction.update(groupRef, newOwnerData)
                            return@runTransaction
                        }.addOnSuccessListener{
                            Log.d(TAG, "delegate ownership to member successfully")
                            showSnackbar(R.string.delegate_ownership_successfully)
                        }.addOnFailureListener{ exception ->
                            Log.w(TAG, "delegate ownership to member failed", exception)
                            showSnackbar(R.string.delegate_ownership_failed)
                        }
                    })
                    .setNegativeButton(R.string.alert_dialog_cancel, { dialog, id ->
                    }).show()
        }
    }

    override fun deleteMember(item: DocumentSnapshot) {
        item.reference.parent.parent?.let{ groupRef ->
            AlertDialog.Builder(this)
                    .setTitle(R.string.delete_member_dialog_title)
                    .setPositiveButton(R.string.delete_member_positive_button, {
                        dialog, id ->
                        // 자기 자신을 삭제할 수 없음
                        if(item.id == mAuth.currentUser?.uid){
                            Log.w(TAG, "Delete member failed - try to delete myself")
                            showSnackbar(R.string.delete_member_failed)
                            return@setPositiveButton
                        }
                        mDb.runTransaction {transaction ->
                            transaction.delete(mDb.collection(USERS).document(item.id).collection(PARTICIPATED_GROUP).document(groupRef.id))
                            transaction.delete(item.reference)
                            return@runTransaction
                        }.addOnSuccessListener {
                            Log.d(TAG, "Delete member successfully")
                            showSnackbar(R.string.delete_member_successfully)
                        }.addOnFailureListener { exception ->
                            Log.w(TAG, "Delete member failed", exception)
                            showSnackbar(R.string.delete_member_failed)
                        }
                    })
                    .setNegativeButton(R.string.alert_dialog_cancel, {
                        dialog, id ->
                    }).show()
        }
    }

    /**
     * 스낵바 출력
     */
    fun showSnackbar(@StringRes errorMessageRes: Int) {
        Snackbar.make(main_root_layout, errorMessageRes, Snackbar.LENGTH_LONG).show()
    }
}

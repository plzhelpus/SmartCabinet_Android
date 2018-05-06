package org.plzhelpus.smartcabinet_android.main

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
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
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import android.text.TextUtils
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.nav_drawer.*
import kotlinx.android.synthetic.main.nav_header_main.*
import android.support.design.widget.Snackbar
import android.support.annotation.StringRes
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import org.plzhelpus.smartcabinet_android.*
import org.plzhelpus.smartcabinet_android.groupInfo.GroupSettingActivity
import org.plzhelpus.smartcabinet_android.auth.AuthUiActivity
import org.plzhelpus.smartcabinet_android.groupInfo.GroupInfoFragmentPagerAdapter
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*


/**
 * Created by Donghwan Kim on 2018-03-23.
 *
 * 앱의 메인 화면 액티비티
 */

class MainActivity : AppCompatActivity(),
        FirebaseAuth.AuthStateListener,
        RecyclerViewOnListItemClickListener<DocumentSnapshot> {
    private var mGroupListListenerRegistration : ListenerRegistration? = null
    private var mCurrentGroupListenerRegistration : ListenerRegistration? = null
    private var mCurrentGroup : DocumentReference? = null
    private lateinit var mAuth : FirebaseAuth
    private lateinit var mDb : FirebaseFirestore

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
        }

        // 네비게이션 드로어에 사용자 로그아웃 버튼 구현
        user_sign_out_button.setOnClickListener {
            // AuthUI가 로그아웃하는 중에 리스너를 트리거할 수 있기 때문에 미리 해제함.
            mAuth.removeAuthStateListener(this)
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            startActivity(AuthUiActivity.createIntent(this))
                            finish()
                        } else {
                            Log.w(TAG, "signOut:failure", task.exception)
                            showSnackbar(R.string.sign_out_failed)
                        }
                    }
        }
    }

    /**
     * 사물함에 요청
     */
    private fun createNewGroup() {
        Log.d(TAG, "Create group button clicked")
        AlertDialog.Builder(this)
                .setTitle(R.string.create_group_dialog_title)
                .setView(R.layout.dialog_create_group)
                .setPositiveButton(R.string.create_group_positive_button, {
                    dialog, id ->
                    // TODO 그룹 추가 구현
                })
                .setNegativeButton(R.string.create_group_negative_button, {
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
        Log.d(TAG, "Changing group now")
        Log.d(TAG, "document snapshot - ${groupListItemDocumentSnapshot.data}")
        mCurrentGroup = groupListItemDocumentSnapshot.getDocumentReference(GROUP_REF).apply {
            (group_pager.adapter as GroupInfoFragmentPagerAdapter).updateGroupInfo(this)
        }
        group_info_group_name.text = groupListItemDocumentSnapshot.getString(GROUP_NAME)
        registerCurrentGroup()
    }

    /**
     * 유저가 로그인 했다면 네비게이션 드로어의 프로필 칸을 채움.
     */
    private fun updateUserInfoUI(user : FirebaseUser?) {
        if(TextUtils.isEmpty(user?.email)){
            user_email.setText(R.string.no_email)
        } else {
            user_email.text = user!!.email // TextUtils.isEmpty가 user.email의 null 확인도 함.
        }
    }

    /**
     * 현재 보고 있는 그룹에 대한 정보의 변경사항을 계속 받아오게 함.
     */
    private fun registerCurrentGroup() {
        mCurrentGroup?.let{ currentGroup ->
            // 다른 그룹에 대한 변경사항 리스너 등록을 해제해야 함.
            mCurrentGroupListenerRegistration?.remove()
            // 그룹 문서의 변경사항을 받게 함.
            mCurrentGroupListenerRegistration = currentGroup.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let {
                    Log.w(TAG, "Current group - Listen failed.", it)
                    return@addSnapshotListener
                }

                // 문서스냅샷이 엉뚱한 곳을 가리키지 않았을 경우,
                if (documentSnapshot.exists()){
                    Log.d(TAG, "Current group - data found")
                    group_info_owner_email.text = documentSnapshot.getString(OWNER_EMAIL)
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
        // 기존에 있다면 지워야 함.
        mGroupListListenerRegistration?.remove()
        // 그룹 목록이 Firestore의 변경 사항을 받게 등록함.
        mDb.collection(USERS).document(user.uid).collection(PARTICIPATED_GROUP).run{
            mGroupListListenerRegistration = addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let{
                    Log.w(TAG, "Participated group - Listen failed.", it)
                    return@addSnapshotListener
                }

                querySnapshot?.let { participatedGroupsSnapshot ->
                    Log.d(TAG, "Participated group - data found")
                    (group_list.adapter as GroupRecyclerViewAdapter).updateList(participatedGroupsSnapshot.documents)
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
                                    firstSeenGroupName?.let{
                                        // 만약 해당 그룹이 그룹 목록에 존재하면 그 그룹으로 변경함
                                        param.find { document ->
                                            document.getString(GROUP_NAME) == it
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
        mAuth.removeAuthStateListener(this)
        mGroupListListenerRegistration?.remove()
        mCurrentGroupListenerRegistration?.remove()
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.action_add_cabinet -> {
                AlertDialog.Builder(this)
                        .setTitle(R.string.add_cabinet_dialog_title)
                        .setView(R.layout.dialog_add_cabinet)
                        .setPositiveButton(R.string.add_cabinet_positive_button, {
                            dialog, id ->
                            // TODO 사물함 추가 구현
                        })
                        .setNegativeButton(R.string.add_cabinet_negative_button, {
                            dialog, id ->
                        }).show()
                return true
            }
            R.id.action_add_member -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.add_member_dialog_title)
                        .setView(R.layout.dialog_add_member)
                        .setPositiveButton(R.string.add_member_positive_button, {
                            dialog, id ->
                            // TODO 멤버 추가 구현
                        })
                        .setNegativeButton(R.string.add_member_negative_button, {
                            dialog, id ->
                        }).show()
                return true
            }
            R.id.action_settings -> {
                mCurrentGroup?.let{startActivity(GroupSettingActivity.createIntent(this, it.path))}
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onListItemClicked(item: DocumentSnapshot) {
        changeGroupInfo(item)
        drawer_layout.closeDrawers()
    }

    /**
     * 스낵바 출력
     */
    private fun showSnackbar(@StringRes errorMessageRes: Int) {
        Snackbar.make(main_root_layout, errorMessageRes, Snackbar.LENGTH_LONG).show()
    }
}

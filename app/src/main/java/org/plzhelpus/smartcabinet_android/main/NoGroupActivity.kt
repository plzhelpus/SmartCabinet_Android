package org.plzhelpus.smartcabinet_android.main

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.StringRes
import kotlinx.android.synthetic.main.activity_no_group.*
import com.firebase.ui.auth.AuthUI
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.text.TextUtils
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthRecentLoginRequiredException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.android.synthetic.main.dialog_create_group.view.*
import org.plzhelpus.smartcabinet_android.PARTICIPATED_GROUP
import org.plzhelpus.smartcabinet_android.R
import org.plzhelpus.smartcabinet_android.USERS
import org.plzhelpus.smartcabinet_android.auth.AuthUiActivity

/**
 * 속한 그룹이 없을 때, 메인 액티비티 대신 띄워지는 액티비티
 */
class NoGroupActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    private var mGroupListListenerRegistration : ListenerRegistration? = null

    private lateinit var mAuth : FirebaseAuth
    private lateinit var mDb : FirebaseFirestore
    private lateinit var mFunctions : FirebaseFunctions

    companion object {
        private val TAG = "NoGroupActivity"

        fun createIntent(context: Context): Intent {
            return Intent().setClass(context, NoGroupActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_group)

        mAuth = FirebaseAuth.getInstance()
        mDb = FirebaseFirestore.getInstance()
        mFunctions = FirebaseFunctions.getInstance()

        being_new_owner_button.setOnClickListener{
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
                        data.put("groupName", createGroupDialog.create_group_group_name_input.text.toString())
                        mFunctions.getHttpsCallable("createGroup")
                                .call(data)
                                .continueWith { task ->
                                    task.result.data
                                }
                                .addOnSuccessListener {
                                    Log.d(TAG, "Create group successfully")
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

        already_member_but_not_found_group_button.setOnClickListener{
            mAuth.currentUser?.run{
                registerGroupList(this)
            }
        }

        no_group_sign_out_button.setOnClickListener{
            // AuthUI가 로그아웃하는 중에 리스너를 트리거할 수 있기 때문에 미리 해제함.
            mGroupListListenerRegistration?.remove()
            mAuth.removeAuthStateListener(this)
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            handleNotSignIn()
                        } else {
                            Log.w(TAG, "Sign out failed - ", task.exception)
                            showSnackbar(R.string.sign_out_failed)
                            mAuth.addAuthStateListener(this)
                        }
                    }
        }

        delete_account_with_no_group_button.setOnClickListener {
            AlertDialog.Builder(this)
                    .setMessage(R.string.delete_account_alert_text)
                    .setPositiveButton(R.string.delete_account_alert_positive_button, { _, _ -> deleteAccount() })
                    .setNegativeButton(R.string.alert_dialog_cancel, null)
                    .show()
        }
    }

    private fun deleteAccount() {
        // AuthUI가 계정을 삭제하는 중에 리스너를 트리거할 수 있기 때문에 미리 해제함.
        mGroupListListenerRegistration?.remove()
        mAuth.removeAuthStateListener(this)
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        handleNotSignIn()
                    } else {
                        Log.w(TAG, "Delete account failed - ", task.exception)
                        if(task.exception is FirebaseAuthRecentLoginRequiredException){
                            AlertDialog.Builder(this)
                                    .setTitle(R.string.need_reauthentication_title)
                                    .setMessage(R.string.need_reauthentication_content)
                                    .setPositiveButton(R.string.alert_dialog_ok, {_, _ -> })
                                    .show()
                            return@addOnCompleteListener
                        }
                        showSnackbar(R.string.delete_account_failed)
                        mAuth.addAuthStateListener(this)
                    }
                }
    }

    private fun handleNotSignIn() {
        startActivity(AuthUiActivity.createIntent(this))
        finish()
    }


    override fun onStart() {
        super.onStart()
        // 유저가 로그인 했는지 확인
        mAuth.addAuthStateListener(this)
    }

    override fun onStop() {
        super.onStop()
        mGroupListListenerRegistration?.remove()
        mAuth.removeAuthStateListener(this)
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val user = firebaseAuth.currentUser
        Log.d(TAG, "login - " + (user?.uid ?: "null"))
        if(user == null){
            Log.w(TAG, "Auth has changed")
            handleNotSignIn()
            return
        }
        registerGroupList(user)
    }

    /**
     * 그룹 목록의 변경을 듣는 리스너를 등록함
     */
    private fun registerGroupList(user: FirebaseUser) {
        // 기존에 있다면 지워야 함.
        mGroupListListenerRegistration?.remove()
        // 그룹 목록이 Firestore의 변경 사항을 받게 등록함.
        mDb.collection(USERS).document(user.uid).collection(PARTICIPATED_GROUP).run{
            mGroupListListenerRegistration = addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let{ exception ->
                    Log.w(TAG, "Participated group - Listen failed.", exception)
                    return@addSnapshotListener
                }
                querySnapshot?.let { participatedGroupsSnapshot ->
                    Log.d(TAG, "Participated group - data found")
                    if (!participatedGroupsSnapshot.isEmpty) {
                        startActivity(MainActivity.createIntent(this@NoGroupActivity))
                        finish()
                    }
                }?. run {
                    Log.d(TAG, "Participated group - data null")
                }
            }
        }

    }

    /**
     * 스낵바를 띄워줌
     */
    private fun showSnackbar(@StringRes content: Int) {
        Snackbar.make(no_group_root_layout, content, Snackbar.LENGTH_LONG).show()
    }
}

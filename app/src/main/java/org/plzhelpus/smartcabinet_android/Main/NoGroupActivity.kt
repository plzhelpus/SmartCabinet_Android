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
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import org.plzhelpus.smartcabinet_android.PARTICIPATED_GROUP
import org.plzhelpus.smartcabinet_android.R
import org.plzhelpus.smartcabinet_android.USERS
import org.plzhelpus.smartcabinet_android.auth.AuthUiActivity

/**
 * 속한 그룹이 없을 때, 메인 액티비티 대신 띄워지는 액티비티
 */
class NoGroupActivity : AppCompatActivity(), FirebaseAuth.AuthStateListener {

    private lateinit var mAuth : FirebaseAuth

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

        being_new_owner_button.setOnClickListener{
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

        already_member_but_not_found_group_button.setOnClickListener{
            val currentUser = mAuth.currentUser
            val db = FirebaseFirestore.getInstance()
            val collectionReference = db.collection(USERS).document(currentUser!!.uid).collection(PARTICIPATED_GROUP)
            collectionReference.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val querySnapshot = it.result
                    // 그룹 목록이 없다면
                    if (querySnapshot.isEmpty) {
                        showSnackbar(R.string.group_list_is_empty)
                    } else {
                        startActivity(MainActivity.createIntent(this))
                        finish()
                    }
                } else {
                        Log.d(TAG, "get group list failed - ", it.exception)
                        showSnackbar(R.string.group_list_refresh_failed)
                }
            }
        }

        no_group_sign_out_button.setOnClickListener{
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

        delete_account_with_no_group_button.setOnClickListener {
            AlertDialog.Builder(this)
                    .setMessage(R.string.delete_account_alert_text)
                    .setPositiveButton(R.string.delete_account_alert_positive_button, { _, _ -> deleteAccount() })
                    .setNegativeButton(R.string.delete_account_alert_negative_button, null)
                    .show()
        }
    }

    private fun deleteAccount() {
        // AuthUI가 계정을 삭제하는 중에 리스너를 트리거할 수 있기 때문에 미리 해제함.
        mAuth.removeAuthStateListener(this)
        AuthUI.getInstance()
                .delete(this)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        startActivity(AuthUiActivity.createIntent(this))
                        finish()
                    } else {
                        showSnackbar(R.string.delete_account_failed)
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
        mAuth.removeAuthStateListener(this)
    }

    override fun onAuthStateChanged(firebaseAuth: FirebaseAuth) {
        val user = firebaseAuth.currentUser
        Log.d(TAG, "login - " + (user?.uid ?: "null"))
        if(user == null){
            handleNotSignIn()
            return
        }

    }

    /**
     * 스낵바를 띄워줌
     */
    private fun showSnackbar(@StringRes content: Int) {
        Snackbar.make(no_group_root_layout, content, Snackbar.LENGTH_LONG).show()
    }
}

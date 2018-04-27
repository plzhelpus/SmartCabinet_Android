package org.plzhelpus.smartcabinet_android.main

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.StringRes
import com.firebase.ui.auth.IdpResponse
import kotlinx.android.synthetic.main.activity_no_group.*
import com.firebase.ui.auth.AuthUI
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import org.plzhelpus.smartcabinet_android.R
import org.plzhelpus.smartcabinet_android.auth.AuthUiActivity


class NoGroupActivity : AppCompatActivity() {

    private var mIdpResponse: IdpResponse? = null

    companion object {
        private val TAG = "NoGroupActivity"
        private val EXTRA_IDP_RESPONSE = "extra_idp_response"

        fun createIntent(context: Context, idpResponse: IdpResponse?): Intent {
            val startIntent: Intent = Intent()
            if(idpResponse != null) {
                startIntent.putExtra(EXTRA_IDP_RESPONSE, idpResponse)
            }
            return startIntent.setClass(context, NoGroupActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_no_group)

        being_new_owner_button.setOnClickListener{
            // TODO 연결 버튼과 같이 설정
        }

        already_member_but_not_found_group_button.setOnClickListener{
            val currentUser = FirebaseAuth.getInstance().currentUser
            val db = FirebaseFirestore.getInstance()
            val collectionReference = db.collection("users").document(currentUser!!.uid).collection("participated_group")
            collectionReference.get().addOnCompleteListener {
                if (it.isSuccessful) {
                    val querySnapshot = it.result
                    // 그룹 목록이 없다면
                    if (querySnapshot.isEmpty) {
                        showSnackbar(R.string.group_list_is_empty)
                    } else {
                        startActivity(MainActivity.createIntent(this, mIdpResponse))
                        finish()
                    }
                } else {
                        Log.d(TAG, "get group list failed - ", it.exception)
                        showSnackbar(R.string.group_list_refresh_failed)
                }
            }
        }

        no_group_sign_out_button.setOnClickListener{
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

    override fun onResume() {
        super.onResume()
        // 유저가 로그인 했는지 확인
        val currentUser : FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            handleNotSignIn()
        }
    }

    /**
     * 스낵바를 띄워줌
     */
    private fun showSnackbar(@StringRes content: Int) {
        Snackbar.make(no_group_root_layout, content, Snackbar.LENGTH_LONG).show()
    }
}

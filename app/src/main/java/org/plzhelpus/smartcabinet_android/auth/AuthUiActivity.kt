package org.plzhelpus.smartcabinet_android.auth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.util.Log
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.ErrorCodes
import com.firebase.ui.auth.IdpResponse
import kotlinx.android.synthetic.main.activity_auth_ui.*
import java.util.*
import com.google.firebase.auth.FirebaseAuth
import org.plzhelpus.smartcabinet_android.R
import org.plzhelpus.smartcabinet_android.main.MainActivity

/**
 * 사용자 로그인을 위한 액티비티
 */
class AuthUiActivity : AppCompatActivity() {

    private lateinit var mAuth : FirebaseAuth

    companion object {
        private val TAG = "AuthUiActivity"
        // startActivityForResult의 결과 코드
        private val RC_SIGN_IN = 200

        fun createIntent(context: Context): Intent{
            return Intent(context, AuthUiActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_auth_ui)

        mAuth = FirebaseAuth.getInstance()

        user_sign_in_button.setOnClickListener{
            startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                AuthUI.IdpConfig.GoogleBuilder().build()))
                        .build(),
                    RC_SIGN_IN)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            handleSignInResponse(resultCode, data)
        } else {
            showSnackbar(R.string.unknown_response)
        }
    }

    private fun handleSignInResponse(resultCode: Int, data: Intent?) {
        val response: IdpResponse? = IdpResponse.fromResultIntent(data)
        if(response == null) {
            showSnackbar(R.string.sign_in_cancelled)
            return
        }
        // 성공적으로 로그인
        if (resultCode == Activity.RESULT_OK){
            backToMainActivity()
        } else {
            // 로그인 실패
            if(response.error?.errorCode == ErrorCodes.NO_NETWORK) {
                showSnackbar(R.string.no_internet_connection)
            } else if(response.error?.message == "Code: 12501, message: 12501: "){
                // 계정 선택 창을 취소할 때 보이는 에러 메세지인데 정확히 무슨 오류인지는 확인 안됨.
                showSnackbar(R.string.sign_in_cancelled)
            } else {
                showSnackbar(R.string.unknown_error)
                Log.e(TAG, "Sign-in error: ", response.error)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (mAuth.currentUser != null) {
            backToMainActivity()
        }
    }

    private fun backToMainActivity(){
        startActivity(MainActivity.createIntent(this))
        finish()
    }

    /**
     * 스낵바를 띄워줌
     */
    private fun showSnackbar(@StringRes content: Int) {
        Snackbar.make(auth_ui_root_layout, content, Snackbar.LENGTH_LONG).show()
    }
}

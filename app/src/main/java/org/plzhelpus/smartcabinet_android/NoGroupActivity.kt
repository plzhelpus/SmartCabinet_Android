package org.plzhelpus.smartcabinet_android

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import com.firebase.ui.auth.IdpResponse
import kotlinx.android.synthetic.main.activity_no_group.*

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
            // TODO 그룹 리스트 갱신 + 그룹 리스트 존재하는지만 쿼리 할 수 있는지 찾아봐야함
        }

        delete_account_with_no_group_button.setOnClickListener {
            // TODO 계정 삭제 버튼
        }
    }
}

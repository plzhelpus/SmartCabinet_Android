package org.plzhelpus.smartcabinet_android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.util.*
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.util.Log
import android.widget.ArrayAdapter
import com.firebase.ui.auth.ErrorCodes
import com.google.firebase.auth.FirebaseUser
import kotlinx.android.synthetic.main.nav_drawer.*
import kotlinx.android.synthetic.main.nav_header_main.*

/**
 * Created by Donghwan Kim on 2018-03-23.
 *
 * 앱의 메인 화면 액티비티
*/

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private val RC_SIGN_IN = 200

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        cabinet_request_button.setOnClickListener(){_ ->
            Log.d(TAG, "Cabinet request button clicked")
        }

        // 유저가 로그인했는지 확인
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            user_email.setText(R.string.not_sign_in)
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    AuthUI.IdpConfig.GoogleBuilder().build()))
                            .build(),
                    RC_SIGN_IN)
        } else {
            polulateProfile()
            val values = arrayOf("Group A", "Group B", "Group C", "Group D", "Group E", "Group F",
                    "Group G", "Group H", "Group I", "Group J", "Group K", "Group L", "Group M", "Group N")
            val groupsAdapter: ArrayAdapter<String> = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, values)
            group_list.adapter = groupsAdapter
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // RC_SIGN_IN 은 로그인 절차를 시작할 때 당신이 startActivityForResult(...) 안으로 전해준 요청 코드다.
        if (requestCode == RC_SIGN_IN) {
            val response: IdpResponse
            try{
                response = IdpResponse.fromResultIntent(data)!!
            } catch(e: NullPointerException) {
                showSnackbar(R.string.sign_in_cancelled)
                return
            }
            // 성공적으로 로그인
            if (resultCode == Activity.RESULT_OK){
                polulateProfile()
            } else {
                // 로그인 실패
                if(response.error?.errorCode == ErrorCodes.NO_NETWORK){
                    showSnackbar(R.string.no_internet_connection)
                    return
                }
                showSnackbar(R.string.unknown_error)
                Log.e(TAG, "Sign-in error: ", response.error)
            }
        } else {
            showSnackbar(R.string.unknown_response)
        }
    }

    /**
     * 스낵바를 띄워줌
     */
    private fun showSnackbar(@StringRes content: Int) {
        Snackbar.make(content_layout, content, Snackbar.LENGTH_LONG).show()
    }

    /**
     * 유저가 로그인 했다면 프로필 칸을 채움.
     */
    private fun polulateProfile() {
        val user: FirebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        user_email.text = if (TextUtils.isEmpty(user.email)) "No email" else user.email
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    override fun onResume() {
        super.onResume()

        // 유저가 로그인 했는지 확인
        val currentUser : FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            user_email.setText(R.string.not_sign_in)
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .setAvailableProviders(Arrays.asList(
                                    AuthUI.IdpConfig.GoogleBuilder().build()))
                            .build(),
                    RC_SIGN_IN)
        } else {
            polulateProfile()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        when (item.itemId) {
            R.id.action_settings -> return true
            else -> return super.onOptionsItemSelected(item)
        }
    }
}

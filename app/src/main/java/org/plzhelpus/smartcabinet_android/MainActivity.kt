package org.plzhelpus.smartcabinet_android

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.app_bar_main.*
import java.util.*
import android.support.design.widget.Snackbar
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.TextView
import com.firebase.ui.auth.ErrorCodes
import com.google.firebase.auth.FirebaseUser

/**
 * Created by Donghwan Kim on 2018-03-23.
 *
 * 앱의 런쳐 액티비티
*/

class MainActivity : AppCompatActivity() {
    private val TAG = "MainActivity"

    private val RC_SIGN_IN = 200
    private val RC_SIGN_IN_FAIL = 403

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val currentUser : FirebaseUser? = FirebaseAuth.getInstance().currentUser

        if (currentUser == null) {
            startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(Arrays.asList(
                                AuthUI.IdpConfig.GoogleBuilder().build()))
                        .build(),
                RC_SIGN_IN)
        }

        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this, drawer_layout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        val cabinetRequestButton : Button = findViewById(R.id.cabinet_request_button)
        cabinetRequestButton.setOnClickListener(CabinetRequestOnClickListener())
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        // RC_SIGN_IN 은 로그인 절차를 시작할 때 당신이 startActivityForResult(...) 안으로 전해준 요청 코드다.
        var mContentLayout: View = findViewById(R.id.content_layout)
        if (requestCode == RC_SIGN_IN) {
            var response: IdpResponse
            try{
                response = IdpResponse.fromResultIntent(data)!!
            }catch(e: NullPointerException){
                Snackbar.make(mContentLayout,"sign in cancelled" ,Snackbar.LENGTH_LONG)
                return
            }
            // 성공적으로 로그인
            if (resultCode == Activity.RESULT_OK){
                TODO("not implemented") //성공적으로 로그인했을 때의 절차 표시
            } else {
                // 로그인 실패
                if(response.error?.errorCode == ErrorCodes.NO_NETWORK){
                    Snackbar.make(mContentLayout, "no network", Snackbar.LENGTH_LONG)
                    return
                }
                Snackbar.make(mContentLayout, "unknown error", Snackbar.LENGTH_LONG)
                Log.e(TAG, "Sign-in error: ", response.error)
            }
        }
    }

    private fun polulateProfile() {
        var user: FirebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        var userEmail:TextView = findViewById(R.id.user_email)
        userEmail.setText( if (TextUtils.isEmpty(user.email)) "No email" else user.email)
    }

    override fun onBackPressed() {
        if (drawer_layout.isDrawerOpen(GravityCompat.START)) {
            drawer_layout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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

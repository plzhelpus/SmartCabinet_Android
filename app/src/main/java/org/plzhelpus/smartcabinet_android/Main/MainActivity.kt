package org.plzhelpus.smartcabinet_android.Main

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.support.v4.view.GravityCompat
import android.support.v7.app.ActionBarDrawerToggle
import android.support.v7.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import com.firebase.ui.auth.IdpResponse
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
import android.support.v7.widget.LinearLayoutManager
import org.plzhelpus.smartcabinet_android.Auth.AuthUiActivity
import org.plzhelpus.smartcabinet_android.Cabinet.NewCabinetActivity
import org.plzhelpus.smartcabinet_android.GroupInfo.CabinetFragment
import org.plzhelpus.smartcabinet_android.GroupInfo.GroupInfoFragmentPagerAdapter
import org.plzhelpus.smartcabinet_android.GroupInfo.MemberFragment
import org.plzhelpus.smartcabinet_android.R
import org.plzhelpus.smartcabinet_android.dummy.DummyCabinet
import org.plzhelpus.smartcabinet_android.dummy.DummyGroup
import org.plzhelpus.smartcabinet_android.dummy.DummyMember


/**
 * Created by Donghwan Kim on 2018-03-23.
 *
 * 앱의 메인 화면 액티비티
*/

class MainActivity : AppCompatActivity(),
        CabinetFragment.OnListFragmentInteractionListener,
        MemberFragment.OnListFragmentInteractionListener {
    private var mIdpResponse: IdpResponse? = null

    companion object {
        private val TAG = "MainActivity"
        private val EXTRA_IDP_RESPONSE = "extra_idp_response"

        fun createIntent(context: Context, idpResponse: IdpResponse?): Intent{
            val startIntent: Intent = Intent()
            if(idpResponse != null) {
                startIntent.putExtra(EXTRA_IDP_RESPONSE, idpResponse)
            }
            return startIntent.setClass(context, MainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        val toggle = ActionBarDrawerToggle(
                this,
                drawer_layout,
                toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close)
        drawer_layout.addDrawerListener(toggle)
        toggle.syncState()

        // 유저가 로그인했는지 확인
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
        if (currentUser == null) {
            handleNotSignIn()
        } else {
            populateProfile()
            mIdpResponse = intent.getParcelableExtra(EXTRA_IDP_RESPONSE)

            // TODO 만약 그룹이 있으면 그룹 첫번째를 열어주고 아니면 빈 그룹 열어주기
            if(DummyGroup.ITEMS.size == 0){
                startActivity(NoGroupActivity.createIntent(this, mIdpResponse))
                finish()
                return
            }
        }

        cabinet_request_button.setOnClickListener{
            startActivity(NewCabinetActivity.createIntent(this))
        }

        user_sign_out_button.setOnClickListener{
            AuthUI.getInstance()
                    .signOut(this)
                    .addOnCompleteListener{
                        task -> if(task.isSuccessful) {
                            startActivity(AuthUiActivity.createIntent(this))
                            finish()
                        } else {
                            Log.w(TAG, "signOut:failure", task.exception)
                        showSnackbar(R.string.sign_out_failed)
                        }
                    }
        }

        group_list.layoutManager = LinearLayoutManager(this)
        group_list.adapter = GroupRecyclerViewAdapter(DummyGroup.ITEMS, this)

        val groupInfoFragmentPagerAdapter = GroupInfoFragmentPagerAdapter(supportFragmentManager)
        group_pager.adapter = groupInfoFragmentPagerAdapter

        // 그룹 탭에 아이콘 삽입
        group_tablayout.setupWithViewPager(group_pager)
        for ((index, resId) in groupInfoFragmentPagerAdapter.tabIconResId.withIndex()) {
            group_tablayout.getTabAt(index)?.setIcon(resId)
        }
    }

    private fun handleNotSignIn(){
        startActivity(AuthUiActivity.createIntent(this))
        finish()
        return
    }

    /**
     * 유저가 로그인 했다면 프로필 칸을 채움.
     */
    private fun populateProfile() {
        val user: FirebaseUser = FirebaseAuth.getInstance().currentUser ?: return
        val email = if (TextUtils.isEmpty(user.email)) "No email" else user.email!!
        // TODO 이메일 앞부분만 따서 출력 중
        user_email.text = (email.split(delimiters = *charArrayOf('@')))[0]
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
            handleNotSignIn()
        } else {
            populateProfile()
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

    override fun onListFragmentInteraction(item: DummyMember.DummyItem) {
        // TODO 리스트 프래그먼트 MemberFragment
    }

    override fun onListFragmentInteraction(item: DummyCabinet.DummyItem) {
        // TODO 리스트 프래그먼트 CabinetFragment
    }

    fun onListFragmentInteraction(item: DummyGroup.DummyItem) {
        this.title = item.content
        drawer_layout.closeDrawers()
    }

    private fun showSnackbar(@StringRes errorMessageRes: Int) {
        Snackbar.make(main_root_layout, errorMessageRes, Snackbar.LENGTH_LONG).show()
    }
}

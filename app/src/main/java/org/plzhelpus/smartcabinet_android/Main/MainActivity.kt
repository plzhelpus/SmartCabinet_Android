package org.plzhelpus.smartcabinet_android.main

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothSocket
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
import android.support.v7.app.AlertDialog
import android.support.v7.widget.LinearLayoutManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import org.plzhelpus.smartcabinet_android.groupInfo.GroupSettingActivity
import org.plzhelpus.smartcabinet_android.R
import org.plzhelpus.smartcabinet_android.auth.AuthUiActivity
import org.plzhelpus.smartcabinet_android.dummy.DummyCabinet
import org.plzhelpus.smartcabinet_android.dummy.DummyMember
import org.plzhelpus.smartcabinet_android.groupInfo.CabinetFragment
import org.plzhelpus.smartcabinet_android.groupInfo.GroupInfoFragmentPagerAdapter
import org.plzhelpus.smartcabinet_android.groupInfo.MemberFragment
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
        CabinetFragment.OnListFragmentInteractionListener,
        MemberFragment.OnListFragmentInteractionListener {
    private var mIdpResponse: IdpResponse? = null
    private var mGroupListenerRegistration: ListenerRegistration? = null

    companion object {
        private val TAG = "MainActivity"
        private val EXTRA_IDP_RESPONSE = "extra_idp_response"

        fun createIntent(context: Context, idpResponse: IdpResponse?): Intent {
            val startIntent: Intent = Intent()
            if (idpResponse != null) {
                startIntent.putExtra(EXTRA_IDP_RESPONSE, idpResponse)
            }
            return startIntent.setClass(context, MainActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar)

        // 네이게이션 드로어 설정
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

            val db = FirebaseFirestore.getInstance()
            val collectionReference = db.collection("users").document(currentUser!!.uid).collection("participated_group")
            collectionReference.get().addOnCompleteListener {
                if(it.isSuccessful){
                    val querySnapshot = it.result
                    if(querySnapshot.isEmpty){
                        startActivity(NoGroupActivity.createIntent(this, mIdpResponse))
                        finish()
                        return@addOnCompleteListener
                    } else {
                        val documentSnapshots = querySnapshot.documents
                        group_list.adapter = GroupRecyclerViewAdapter(documentSnapshots, this)
                        this.title = documentSnapshots[0].id
                        // TODO 그룹 정보 UI도 갱신 필요
                    }
                } else {
                    Log.d(TAG, "get group list failed - ", it.exception)
                }
            }
        }

        cabinet_request_button.setOnClickListener{
            // startActivity(NewCabinetActivity.createIntent(this))
            // TODO : Recatoring
            val RPI3ADDRESS = "B8:27:EB:21:B6:12"
            val mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter()
            val MY_UUID_SECURE = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB")
            val rpiDevice = mBluetoothAdapter.getRemoteDevice(RPI3ADDRESS)
            val mmSocket: BluetoothSocket
            val mmInStream: InputStream
            val mmOutStream: OutputStream
            var buffer = ByteArray(1024)

            try {
                mmSocket = rpiDevice.createRfcommSocketToServiceRecord(
                        MY_UUID_SECURE)
                mmSocket.connect()

                mmInStream = mmSocket.getInputStream()
                mmOutStream = mmSocket.getOutputStream()

                mmOutStream.write("TEST".toByteArray())

                val bytes = mmInStream.read(buffer)

                Log.i(TAG, "Get data" + String(buffer))

                mmSocket.close()
            } catch (e: IOException) {
                Log.e(TAG, "IOException :", e);
            }

            Log.d(TAG, "Cabinet request button clicked")
        }

        user_sign_out_button.setOnClickListener {
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

        group_list.layoutManager = LinearLayoutManager(this)

        val groupInfoFragmentPagerAdapter = GroupInfoFragmentPagerAdapter(supportFragmentManager)
        group_pager.adapter = groupInfoFragmentPagerAdapter

        // 그룹 탭에 아이콘 삽입
        group_tablayout.setupWithViewPager(group_pager)
        for ((index, resId) in groupInfoFragmentPagerAdapter.tabIconResId.withIndex()) {
            group_tablayout.getTabAt(index)?.setIcon(resId)
        }
    }

    override fun onStart() {
        super.onStart()
        /*val user = FirebaseAuth.getInstance().currentUser
        val db = FirebaseFirestore.getInstance()
        if(user != null){
            val participatedGroup = db.collection("users").document(user.uid).collection("participated_group")
            mGroupListenerRegistration = participatedGroup.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null){
                    Log.w(TAG, "Participated group - Listen failed.", firebaseFirestoreException)
                    return@addSnapshotListener
                }

                if (querySnapshot != null && !querySnapshot.isEmpty){
                    Log.d(TAG, "Participated group - data found")
                    querySnapshot.documents
                } else {
                    Log.d(TAG, "Participated group - data null")
                }
            }
        }*/
    }

    override fun onStop() {
        super.onStop()
        mGroupListenerRegistration?.remove()
    }

    private fun handleNotSignIn() {
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
        user_email.text = email
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
        val currentUser: FirebaseUser? = FirebaseAuth.getInstance().currentUser
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
            R.id.action_add_member -> {
                val builder: AlertDialog.Builder = AlertDialog.Builder(this)
                builder.setTitle(R.string.add_member_dialog_title)
                        .setView(R.layout.dialog_add_member)
                        .setPositiveButton(R.string.add_member_alert_positive_button, {
                            dialog, id ->

                        })
                        .setNegativeButton(R.string.add_member_alert_negative_button, {
                            dialog, id ->
                        }).show()

                return true
            }
            R.id.action_settings -> {
                startActivity(GroupSettingActivity.createIntent(this))
                return true
            }
            else -> return super.onOptionsItemSelected(item)
        }
    }

    override fun onListFragmentInteraction(item: DummyMember.DummyItem) {
        // TODO 리스트 프래그먼트 MemberFragment
    }

    override fun onListFragmentInteraction(item: DummyCabinet.DummyItem) {
        // TODO 리스트 프래그먼트 CabinetFragment
    }

    fun onListFragmentInteraction(item: DocumentSnapshot) {
        this.title = item.id
        drawer_layout.closeDrawers()
    }

    private fun showSnackbar(@StringRes errorMessageRes: Int) {
        Snackbar.make(main_root_layout, errorMessageRes, Snackbar.LENGTH_LONG).show()
    }
}

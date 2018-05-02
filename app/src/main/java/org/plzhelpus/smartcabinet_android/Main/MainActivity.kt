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
import org.plzhelpus.smartcabinet_android.groupInfo.GroupSettingActivity
import org.plzhelpus.smartcabinet_android.R
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
        FirebaseAuth.AuthStateListener {
    private var mGroupListListenerRegistration : ListenerRegistration? = null
    private var mCurrentGroupListenerRegistration : ListenerRegistration? = null
    private var mCurrentGroup : DocumentReference? = null
    private lateinit var mAuth : FirebaseAuth
    private lateinit var mDb : FirebaseFirestore

    companion object {
        private val TAG = "MainActivity"
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
        } ?: run { handleNotSignIn() }

        // 사물함 요청 버튼 구현
        cabinet_request_button.setOnClickListener{
            requestCabinet()
        }

        // 사용자 로그아웃 버튼 구현
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
    private fun requestCabinet() {
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


    /**
     * 속한 그룹이 없는 유저일 때를 처리하는 메서드
     */
    private fun handleNoGroups() {
        startActivity(NoGroupActivity.createIntent(this))
        finish()
    }

    /**
     * 가입한 적 없는 계정일 때를 처리하는 메서드
     */
    private fun handleNotSignIn() {
        startActivity(AuthUiActivity.createIntent(this))
        finish()
    }

    /**
     * 보여주고 있는 그룹을 변경함.
     */
    private fun changeGroupInfo(groupListItemDocumentSnapshot : DocumentSnapshot) {
        Log.d(TAG, "Changing group now")
        Log.d(TAG, "document snapshot - ${groupListItemDocumentSnapshot.data}")
        val newGroupDocumentReference = groupListItemDocumentSnapshot.getDocumentReference("group_ref")
        mCurrentGroup = newGroupDocumentReference.apply {
            (group_pager.adapter as GroupInfoFragmentPagerAdapter).updateGroupInfo(this)
        }
        group_info_group_name.text = newGroupDocumentReference.id
        registerCurrentGroup()
    }

    /**
     * 유저가 로그인 했다면 프로필 칸을 채움.
     */
    private fun updateUserInfoUI(user : FirebaseUser?) {
        if(TextUtils.isEmpty(user?.email)){
            user_email.setText(R.string.no_email)
        } else {
            user_email.text = user!!.email // TextUtils.isEmpty가 user.email의 null 확인도 함.
        }
    }

    private fun registerCurrentGroup() {
        mCurrentGroup?.let{ currentGroup ->
            // 기존에 있다면 지워야 함.
            mCurrentGroupListenerRegistration?.remove()
            // 그룹 문서의 변경사항을 받게 함.
            mCurrentGroupListenerRegistration = currentGroup.addSnapshotListener { documentSnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.run {
                    Log.w(TAG, "Current group - Listen failed.", this)
                    return@addSnapshotListener
                }

                // 문서스냅샷이 엉뚱한 곳을 가리키지 않았을 경우,
                if (documentSnapshot.exists()){
                    Log.d(TAG, "Current group - data found")
                    if(documentSnapshot.contains("owner_email")){
                        group_info_owner_email.text = documentSnapshot.getString("owner_email")
                    }
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
        mDb.collection("users").document(user.uid).collection("participated_group").run{
            mGroupListListenerRegistration = addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.run{
                    Log.w(TAG, "Participated group - Listen failed.", this)
                    return@addSnapshotListener
                }

                querySnapshot?.run {
                    Log.d(TAG, "Participated group - data found")
                    (group_list.adapter as GroupRecyclerViewAdapter).updateList(documents)
                    if (isEmpty) {
                        handleNoGroups()
                    } else {
                        // 어느 그룹을 보여줄 지 정해줘야 함.
                        object : AsyncTask<DocumentSnapshot, Unit, DocumentSnapshot>() {
                            override fun doInBackground(vararg param: DocumentSnapshot): DocumentSnapshot {
                                mCurrentGroup?.let{ currentGroup ->
                                    // 만약 보고 있던 그룹이 삭제되었다면, 첫 번째 그룹으로 변경해줌.
                                    if (param.none { document ->
                                                currentGroup.id == document.id
                                            }) {
                                        Log.d(TAG, "Group that you was watching is not visible now")
                                        return param[0]
                                    }
                                } ?: let{
                                    // 만약 처음 보여주어야 할 그룹이 지정되어 있다면 해당 그룹을 보여줌.
                                    firstSeenGroupName?.let{
                                        param.find { document ->
                                            document.getString("group_name") == it
                                        }?.run{
                                            return this
                                        }
                                    }
                                }
                                return param[0]
                            }

                            override fun onPostExecute(result: DocumentSnapshot) {
                                super.onPostExecute(result)
                                changeGroupInfo(result)
                            }
                        }.execute(*(documents.toTypedArray()))
                    }
                }?. let {
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
        firebaseAuth.currentUser?.let{
            registerGroupList(it)
            updateUserInfoUI(it)
        } ?: run {
            handleNotSignIn()
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
        // TODO 메뉴 관리를 다른 프레그먼트에게 넘길 수 있는지 조사
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

    /**
     * 그룹 목록에서 한 그룹을 선택했을 때, 호출
     */
    fun onGroupListItemClicked(item: DocumentSnapshot) {
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

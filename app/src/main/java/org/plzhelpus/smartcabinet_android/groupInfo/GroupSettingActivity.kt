package org.plzhelpus.smartcabinet_android.groupInfo

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.settings_group_admin.*
import kotlinx.android.synthetic.main.settings_group_member.*
import kotlinx.android.synthetic.main.settings_group_owner.*
import org.plzhelpus.smartcabinet_android.GROUP_REF
import org.plzhelpus.smartcabinet_android.R


/**
 * 그룹 설정을 보여주는 액티비티
 */
class GroupSettingActivity : AppCompatActivity() {

    private var mGroupRef : DocumentReference? = null

    companion object {
        private val TAG = "GroupSettingsActivity"

        fun createIntent(context: Context, groupRef : String): Intent{
            val startIntent: Intent = Intent()
            startIntent.putExtra(GROUP_REF, groupRef)
            return startIntent.setClass(context, GroupSettingActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_setting)
        intent.getStringExtra(GROUP_REF)?.let{ mGroupRef = FirebaseFirestore.getInstance().document(it) }

        settings_leave_group.setOnClickListener {
            Log.d(TAG, "leave group clicked")
            // TODO 서버에서 해야 함.
        }
        settings_demote_self.setOnClickListener {
            Log.d(TAG, "demote self clicked")
        }
        settings_delete_group.setOnClickListener {
            Log.d(TAG, "delete group clicked")
        }
    }
}

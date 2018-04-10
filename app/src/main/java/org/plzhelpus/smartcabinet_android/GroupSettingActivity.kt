package org.plzhelpus.smartcabinet_android

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import kotlinx.android.synthetic.main.settings_group_admin.*
import kotlinx.android.synthetic.main.settings_group_member.*
import kotlinx.android.synthetic.main.settings_group_owner.*

class GroupSettingActivity : AppCompatActivity() {

    companion object {
        private val TAG = "GroupSettingsActivity"

        fun createIntent(context: Context): Intent{
            val startIntent: Intent = Intent()
            return startIntent.setClass(context, GroupSettingActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_setting)

        settings_leave_group.setOnClickListener {
            Log.d(TAG, "leave group clicked")
        }
        settings_change_role_to_member.setOnClickListener {
            Log.d(TAG, "change role to member clicked")
        }
        settings_delete_group.setOnClickListener {
            Log.d(TAG, "delete group clicked")
        }
    }
}

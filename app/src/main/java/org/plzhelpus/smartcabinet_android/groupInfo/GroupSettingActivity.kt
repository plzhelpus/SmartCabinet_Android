package org.plzhelpus.smartcabinet_android.groupInfo

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.support.annotation.StringRes
import android.support.design.widget.Snackbar
import android.support.v7.app.AlertDialog
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.android.synthetic.main.activity_group_setting.*
import kotlinx.android.synthetic.main.settings_group_admin.*
import kotlinx.android.synthetic.main.settings_group_member.*
import kotlinx.android.synthetic.main.settings_group_owner.*
import org.plzhelpus.smartcabinet_android.*


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
            // TODO 그룹 소유자면 탈퇴 불가능
            Log.d(TAG, "leave group clicked")
            AlertDialog.Builder(this)
                    .setTitle(R.string.leave_group_dialog_title)
                    .setPositiveButton(R.string.leave_group_positive_button, {
                        dialog, id ->
                        FirebaseAuth.getInstance().currentUser?.let { user ->
                            mGroupRef?.let { groupRef ->
                                FirebaseFirestore.getInstance()
                                        .collection(USERS)
                                        .document(user.uid)
                                        .collection(PARTICIPATED_GROUP)
                                        .document(groupRef.id).let { groupDocInParticipatedGroup ->
                                            groupDocInParticipatedGroup.delete()
                                                    .addOnSuccessListener {
                                                        Log.d(TAG, "Leave group successfully")
                                                        finish()
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        Log.w(TAG, "Leave group failed", exception)
                                                        showSnackbar(R.string.leave_group_failed)
                                                    }
                                        }
                            }
                        }
                    })
                    .setNegativeButton(R.string.alert_dialog_cancel, {
                        dialog, id ->
                    }).show()
        }
        settings_demote_self.setOnClickListener {
            Log.d(TAG, "demote self clicked")
            // TODO 서버에서 해야 함
        }
        settings_delete_group.setOnClickListener {
            Log.d(TAG, "delete group clicked")
            AlertDialog.Builder(this)
                    .setTitle(R.string.delete_group_dialog_title)
                    .setPositiveButton(R.string.delete_group_positive_button, {
                        dialog, id ->
                        mGroupRef?.let{ groupRef ->
                            groupRef.delete()
                                    .addOnSuccessListener {
                                        Log.d(TAG, "Delete group successfully")
                                        finish()
                                    }
                                    .addOnFailureListener { exception ->
                                        Log.w(TAG, "Error deleting document", exception)
                                        showSnackbar(R.string.delete_group_failed)
                                    }
                        }
                    })
                    .setNegativeButton(R.string.alert_dialog_cancel, {
                        dialog, id ->
                    }).show()
        }

    }

    /**
     * 스낵바를 띄워줌
     */
    private fun showSnackbar(@StringRes content: Int) {
        Snackbar.make(group_setting_root_layout, content, Snackbar.LENGTH_LONG).show()
    }
}

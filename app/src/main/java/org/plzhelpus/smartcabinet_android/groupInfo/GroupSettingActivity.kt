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
import com.google.firebase.functions.FirebaseFunctions
import kotlinx.android.synthetic.main.activity_group_setting.*
import kotlinx.android.synthetic.main.settings_group_member.*
import kotlinx.android.synthetic.main.settings_group_owner.*
import org.plzhelpus.smartcabinet_android.*


/**
 * 그룹 설정을 보여주는 액티비티
 */
class GroupSettingActivity : AppCompatActivity() {

    private var mGroupRef: DocumentReference? = null
    private lateinit var mFunctions: FirebaseFunctions

    companion object {
        private val TAG = "GroupSettingsActivity"
        private val GROUP_ID = "GROUP_ID"

        fun createIntent(context: Context, groupRef: String): Intent {
            val startIntent: Intent = Intent()
            startIntent.putExtra(GROUP_ID, groupRef)
            return startIntent.setClass(context, GroupSettingActivity::class.java)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_setting)
        intent.getStringExtra(GROUP_ID)?.let { mGroupRef = FirebaseFirestore.getInstance().collection(GROUPS).document(it) }
        mFunctions = FirebaseFunctions.getInstance()
        settings_leave_group.setOnClickListener {

            // TODO 테스트 필요
            Log.d(TAG, "leave group clicked")
            AlertDialog.Builder(this)
                    .setTitle(R.string.leave_group_dialog_title)
                    .setPositiveButton(R.string.leave_group_positive_button, { dialog, id ->
                        FirebaseAuth.getInstance().currentUser?.let { user ->
                            mGroupRef?.let { groupRef ->
                                val data: MutableMap<String, Any?> = HashMap()
                                data.put("groupId", groupRef.id)
                                mFunctions
                                        .getHttpsCallable("leaveGroup")
                                        .call(data)
                                        .continueWith { task ->
                                            task.result.data
                                        }
                                        .addOnSuccessListener {
                                            Log.d(TAG, "Leave group success")
                                            finish()
                                        }.addOnFailureListener { exception ->
                                            Log.w(TAG, "Leave group failed", exception)
                                            showSnackbar(R.string.leave_group_failed)
                                        }
                                return@setPositiveButton
                            }
                        }
                    })
                    .setNegativeButton(R.string.alert_dialog_cancel, { dialog, id ->
                    }).show()
        }
        settings_delete_group.setOnClickListener {
            // TODO 테스트 필요
            Log.d(TAG, "delete group clicked")
            AlertDialog.Builder(this)
                    .setTitle(R.string.delete_group_dialog_title)
                    .setPositiveButton(R.string.delete_group_positive_button, { dialog, id ->
                        mGroupRef?.let { groupRef ->
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
                    .setNegativeButton(R.string.alert_dialog_cancel, { dialog, id ->
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

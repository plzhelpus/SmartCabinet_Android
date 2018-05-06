package org.plzhelpus.smartcabinet_android.groupInfo.admin

import android.support.v7.app.AlertDialog
import android.support.v7.util.DiffUtil
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentSnapshot
import org.plzhelpus.smartcabinet_android.R
import kotlinx.android.synthetic.main.admin.view.*
import org.plzhelpus.smartcabinet_android.EMAIL

/**
 * 그룹 관리자 목록을 관리하는 어뎁터
 */
class AdminRecyclerViewAdapter(
        private val mValues: List<DocumentSnapshot>)
    : RecyclerView.Adapter<AdminRecyclerViewAdapter.ViewHolder>() {

    companion object {
        private val TAG = "AdminRecyclerView"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.admin, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mValues[position].let{ item ->
            holder.mView.run {
                tag = item
                admin_email.text = item.getString(EMAIL)
                admin_popup_menu_button.setOnClickListener{
                    val popupMenu =  PopupMenu(context, admin_popup_menu_button)
                    popupMenu.inflate(R.menu.member)
                    popupMenu.setOnMenuItemClickListener {
                        when(it.itemId){
                            R.id.admin_menu_demote_to_member -> {
                                Log.d(TAG, "admin_menu_demote_to_member")
                                // TODO 관리자 권한 낮추는 버튼 구현
                                true
                            }
                            R.id.admin_menu_delegate_owner -> {
                                Log.d(TAG, "admin_menu_delegate_owner")
                                // TODO 관리자에게 소유권 넘기는 버튼 구현
                                true
                            }
                            R.id.admin_menu_delete -> {
                                Log.d(TAG, "admin_menu_delete")
                                AlertDialog.Builder(context)
                                        .setTitle(R.string.delete_admin_dialog_title)
                                        .setPositiveButton(R.string.delete_admin_positive_button, {
                                            dialog, id ->
                                            item.reference.delete()
                                                    .addOnSuccessListener {
                                                        Log.d(TAG, "Delete admin successfully - ${item.getString(EMAIL)}")
                                                    }
                                                    .addOnFailureListener { exception ->
                                                        Log.w(TAG, "Delete admin failed - ${item.getString(EMAIL)}", exception)
                                                    }
                                        })
                                        .setNegativeButton(R.string.alert_dialog_cancel, {
                                            dialog, id ->
                                        }).show()

                                true
                            }
                            else -> false
                        }
                    }
                    popupMenu.show()
                }
            }
        }
    }

    fun updateList(newGroups : List<DocumentSnapshot>){
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(AdminListDiffUtilCallback(mValues, newGroups))
        (mValues as ArrayList).run{
            clear()
            addAll(newGroups)
        }
        diffResult.dispatchUpdatesTo(this)
    }

    override fun getItemCount(): Int = mValues.size

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {

        override fun toString(): String {
            return super.toString() + " '" + mView.admin_email.text + "'"
        }
    }
}

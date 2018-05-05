package org.plzhelpus.smartcabinet_android.groupInfo.member

import android.support.v7.util.DiffUtil
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.member.view.*
import org.plzhelpus.smartcabinet_android.EMAIL
import org.plzhelpus.smartcabinet_android.R

/**
 * 그룹 일반 회원 목록을 관리하는 어뎁터
 */
class MemberRecyclerViewAdapter(private val mValues: List<DocumentSnapshot>) : RecyclerView.Adapter<MemberRecyclerViewAdapter.ViewHolder>() {

    companion object {
        private val TAG = "MemberRecyclerView"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mValues[position].let{ item ->
            holder.mView.run{
                tag = item
                member_email.text = item.getString(EMAIL)
                member_popup_menu_button.setOnClickListener {
                    val popupMenu =  PopupMenu(context, member_popup_menu_button)
                    popupMenu.inflate(R.menu.member)
                    popupMenu.setOnMenuItemClickListener {
                        when(it.itemId){
                            R.id.member_menu_promote_to_admin -> {
                                Log.d(TAG, "member_menu_promote_to_admin")
                                true
                            }
                            R.id.member_menu_delegate_owner -> {
                                Log.d(TAG, "member_menu_delegate_owner")
                                true
                            }
                            R.id.member_menu_delete -> {
                                Log.d(TAG, "member_menu_delete")
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

    override fun getItemCount(): Int = mValues.size

    fun updateList(newGroups : List<DocumentSnapshot>){
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(MemberListDiffUtilCallback(mValues, newGroups))
        (mValues as ArrayList).run{
            clear()
            addAll(newGroups)
        }
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {

        override fun toString(): String {
            return super.toString() + " '" + mView.member_email.text + "'"
        }
    }
}

package org.plzhelpus.smartcabinet_android.groupInfo

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.member.view.*
import org.plzhelpus.smartcabinet_android.R
import org.plzhelpus.smartcabinet_android.dummy.DummyMember.DummyItem

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class MemberRecyclerViewAdapter(private val mValues: List<DummyItem>, private val mContext: Context?) : RecyclerView.Adapter<MemberRecyclerViewAdapter.ViewHolder>() {

    companion object {
        private val TAG = "MemberRecyclerView"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.member, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues.get(position)
        holder.mView.member_email.text = mValues[position].content
        holder.mView.member_role.text = mValues[position].details

        holder.mView.member_popup_menu_button.setOnClickListener {
            if(mContext != null){
                val popupMenu =  PopupMenu(mContext, holder.mView.member_popup_menu_button)
                popupMenu.inflate(R.menu.member)
                popupMenu.setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.member_menu_change_role -> {
                            Log.d(TAG, "member_menu_change_role")
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

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        var mItem: DummyItem? = null

        override fun toString(): String {
            return super.toString() + " '" + mView.member_email.text + "'"
        }
    }
}

package org.plzhelpus.smartcabinet_android.groupInfo

import android.content.Context
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.plzhelpus.smartcabinet_android.dummy.DummyCabinet.DummyItem
import kotlinx.android.synthetic.main.cabinet.view.*
import org.plzhelpus.smartcabinet_android.R

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class CabinetRecyclerViewAdapter(private val mValues: List<DummyItem>, private val mListener: CabinetFragment.OnListFragmentInteractionListener?, private val mContext: Context?) : RecyclerView.Adapter<CabinetRecyclerViewAdapter.ViewHolder>() {

    companion object {
        private val TAG = "CabinetRecyclerView"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cabinet, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mView.cabinet_id.text = mValues[position].content
        holder.mView.cabinet_description.text = mValues[position].details

        holder.mView.cabinet_popup_memu_button.setOnClickListener {
            if (mContext != null){
                val popupMenu =  PopupMenu(mContext, holder.mView.cabinet_popup_memu_button)
                popupMenu.inflate(R.menu.cabinet)
                popupMenu.setOnMenuItemClickListener {
                    when(it.itemId){
                        R.id.cabinet_menu_delete -> {
                            Log.d(TAG, "cabinet_menu_delete")
                            true
                        }
                        R.id.cabinet_menu_edit_description -> {
                            Log.d(TAG, "cabinet_menu_edit_description")
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
            return super.toString() + " '" + mView.cabinet_id + "'"
        }
    }
}

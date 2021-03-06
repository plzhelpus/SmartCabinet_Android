package org.plzhelpus.smartcabinet_android.groupInfo.cabinet

import android.support.v7.util.DiffUtil
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.cabinet.view.*
import org.plzhelpus.smartcabinet_android.DESCRIPTION
import org.plzhelpus.smartcabinet_android.R

/**
 * 그룹 사물함 목록을 관리하는 어뎁터
 */
class CabinetRecyclerViewAdapter(
        private val mValues: List<DocumentSnapshot>,
        private val mHandler: CabinetListItemHandler<DocumentSnapshot>) : RecyclerView.Adapter<CabinetRecyclerViewAdapter.ViewHolder>() {

    companion object {
        private val TAG = "CabinetRecyclerView"
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cabinet, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        mValues[position].let{ item ->
            holder.mView.run{
                tag = item
                cabinet_id.text = item.id
                cabinet_description.text = item.getString(DESCRIPTION)
                cabinet_popup_memu_button.setOnClickListener {
                    val popupMenu =  PopupMenu(context, cabinet_popup_memu_button)
                    popupMenu.inflate(R.menu.cabinet)
                    popupMenu.setOnMenuItemClickListener {
                        when(it.itemId){
                            R.id.cabinet_menu_open_close -> {
                                Log.d(TAG, "cabinet_menu_open")
                                mHandler.openOrCloseCabinet(item)
                                true
                            }
                            R.id.cabinet_menu_delete -> {
                                Log.d(TAG, "cabinet_menu_delete")
                                mHandler.deleteCabinet(item)
                                true
                            }
                            R.id.cabinet_menu_edit_description -> {
                                Log.d(TAG, "cabinet_menu_edit_description")
                                mHandler.editCabinetDescription(item)
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
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(CabinetListDiffUtilCallback(mValues, newGroups))
        (mValues as ArrayList).run{
            clear()
            addAll(newGroups)
        }
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {

        override fun toString(): String {
            return super.toString() + " '" + mView.cabinet_id + "'"
        }
    }
}

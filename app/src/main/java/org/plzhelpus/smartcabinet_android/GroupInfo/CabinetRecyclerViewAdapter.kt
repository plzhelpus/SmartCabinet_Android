package org.plzhelpus.smartcabinet_android.groupInfo

import android.content.Context
import android.support.v7.util.DiffUtil
import android.support.v7.widget.PopupMenu
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentSnapshot
import org.plzhelpus.smartcabinet_android.dummy.DummyCabinet.DummyItem
import kotlinx.android.synthetic.main.cabinet.view.*
import org.plzhelpus.smartcabinet_android.R

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class CabinetRecyclerViewAdapter(private val mValues: List<DocumentSnapshot>) : RecyclerView.Adapter<CabinetRecyclerViewAdapter.ViewHolder>() {

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
                holder.mView.cabinet_id.text = item.id
                holder.mView.cabinet_description.text = item.getString("description")
                cabinet_popup_memu_button.setOnClickListener {
                    val popupMenu =  PopupMenu(context, cabinet_popup_memu_button)
                    popupMenu.inflate(R.menu.cabinet)
                    popupMenu.setOnMenuItemClickListener {
                        when(it.itemId){
                            R.id.cabinet_menu_open -> {
                                Log.d(TAG, "cabinet_menu_open")
                                true
                            }
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

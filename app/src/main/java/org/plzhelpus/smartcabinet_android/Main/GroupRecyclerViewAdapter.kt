package org.plzhelpus.smartcabinet_android.main

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.group.view.*
import org.plzhelpus.smartcabinet_android.R


class GroupRecyclerViewAdapter(private val mValues: List<DocumentSnapshot>,
                               private val mListener: MainActivity?) : RecyclerView.Adapter<GroupRecyclerViewAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mView.group_list_group_name.text = mValues[position].id

        holder.mView.setOnClickListener{
            if (null != mListener) {
                // Notify the active callbacks interface (the activity, if the
                // fragment is attached to one) that an item has been selected.
                mListener.onListFragmentInteraction(holder.mItem!!)
            }
        }
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    fun updateList(newGroups : List<DocumentSnapshot>){
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(GroupListDiffUtilCallback(this.mValues, newGroups))
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        var mItem: DocumentSnapshot? = null

        override fun toString(): String {
            return super.toString() + " '" + mView.group_list_group_name + "'"
        }
    }
}
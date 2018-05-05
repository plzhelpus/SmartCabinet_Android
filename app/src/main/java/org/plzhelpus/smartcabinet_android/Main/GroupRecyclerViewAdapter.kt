package org.plzhelpus.smartcabinet_android.main

import android.support.v7.util.DiffUtil
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentSnapshot
import kotlinx.android.synthetic.main.group.view.*
import org.plzhelpus.smartcabinet_android.R

/**
 * 그룹 목록을 관리하는 어뎁터
 */
class GroupRecyclerViewAdapter(private val mValues: List<DocumentSnapshot>,
                               private val mListener: RecyclerViewOnListItemClickListener<DocumentSnapshot>?) : RecyclerView.Adapter<GroupRecyclerViewAdapter.ViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.group, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        mValues[position].let { item ->
            holder.mView.run {
                tag = item
                group_list_group_name.text = item.id
                setOnClickListener{
                    mListener?.run{
                        onListItemClicked(item)
                    }
                }
            }
        }
    }

    override fun getItemCount(): Int = mValues.size

    fun updateList(newGroups : List<DocumentSnapshot>){
        val diffResult : DiffUtil.DiffResult = DiffUtil.calculateDiff(GroupListDiffUtilCallback(mValues, newGroups))
        (mValues as ArrayList).run{
            clear()
            addAll(newGroups)
        }
        diffResult.dispatchUpdatesTo(this)
    }

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        override fun toString(): String {
            return super.toString() + " '" + mView.group_list_group_name + "'"
        }
    }
}
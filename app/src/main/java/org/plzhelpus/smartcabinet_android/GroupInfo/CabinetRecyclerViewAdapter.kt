package org.plzhelpus.smartcabinet_android.GroupInfo

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import org.plzhelpus.smartcabinet_android.GroupInfo.CabinetFragment.OnListFragmentInteractionListener
import org.plzhelpus.smartcabinet_android.dummy.DummyCabinet.DummyItem
import kotlinx.android.synthetic.main.cabinet.view.*
import org.plzhelpus.smartcabinet_android.R

/**
 * [RecyclerView.Adapter] that can display a [DummyItem] and makes a call to the
 * specified [OnListFragmentInteractionListener].
 * TODO: Replace the implementation with code for your data type.
 */
class CabinetRecyclerViewAdapter(private val mValues: List<DummyItem>, private val mListener: OnListFragmentInteractionListener?) : RecyclerView.Adapter<CabinetRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.cabinet, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mItem = mValues[position]
        holder.mView.cabinet_id.text = mValues[position].content
        holder.mView.cabinet_description.text = mValues[position].details

        holder.mView.setOnClickListener {
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

    inner class ViewHolder(val mView: View) : RecyclerView.ViewHolder(mView) {
        var mItem: DummyItem? = null

        override fun toString(): String {
            return super.toString() + " '" + mView.cabinet_id + "'"
        }
    }
}

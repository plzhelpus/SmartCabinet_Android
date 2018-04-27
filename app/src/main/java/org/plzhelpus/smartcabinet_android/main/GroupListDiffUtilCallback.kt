package org.plzhelpus.smartcabinet_android.main

import android.support.v7.util.DiffUtil
import com.google.firebase.firestore.DocumentSnapshot

class GroupListDiffUtilCallback(
        private val oldDocuments: List<DocumentSnapshot>,
        private val newDocuments : List<DocumentSnapshot>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldDocuments[oldItemPosition].id == newDocuments[newItemPosition].id
    }

    override fun getOldListSize(): Int {
        return oldDocuments.size
    }

    override fun getNewListSize(): Int {
        return newDocuments.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldDocuments[oldItemPosition].get("group_ref") == newDocuments[newItemPosition].get("group_ref")
    }
}
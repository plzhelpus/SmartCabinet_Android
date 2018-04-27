package org.plzhelpus.smartcabinet_android.main

import android.support.v7.util.DiffUtil
import com.google.firebase.firestore.DocumentSnapshot

class GroupListDiffUtilCallback(
        private val mOldDocuments: List<DocumentSnapshot>,
        private val mNewDocuments : List<DocumentSnapshot>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldDocuments[oldItemPosition].id == mNewDocuments[newItemPosition].id
    }

    override fun getOldListSize(): Int {
        return mOldDocuments.size
    }

    override fun getNewListSize(): Int {
        return mNewDocuments.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldDocuments[oldItemPosition].get("group_ref") == mNewDocuments[newItemPosition].get("group_ref")
    }
}
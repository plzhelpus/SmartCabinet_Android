package org.plzhelpus.smartcabinet_android.groupInfo

import android.support.v7.util.DiffUtil
import com.google.firebase.firestore.DocumentSnapshot

class AdminListDiffUtilCallback (
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
        return (mOldDocuments[oldItemPosition].get("email") == mNewDocuments[newItemPosition].get("email")) &&
                (mOldDocuments[oldItemPosition].get("user_ref") == mNewDocuments[newItemPosition].get("user_ref"))
    }
}
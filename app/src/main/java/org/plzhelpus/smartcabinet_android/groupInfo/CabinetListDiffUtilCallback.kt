package org.plzhelpus.smartcabinet_android.groupInfo

import android.support.v7.util.DiffUtil
import com.google.firebase.firestore.DocumentSnapshot

class CabinetListDiffUtilCallback(
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
        return (mOldDocuments[oldItemPosition].get("description") == mNewDocuments[newItemPosition].get("description") &&
                (mOldDocuments[oldItemPosition].get("cabinet_ref") == mNewDocuments[newItemPosition].get("cabinet_ref")))
    }
}
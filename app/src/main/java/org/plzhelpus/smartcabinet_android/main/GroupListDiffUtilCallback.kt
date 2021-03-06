package org.plzhelpus.smartcabinet_android.main

import android.support.v7.util.DiffUtil
import com.google.firebase.firestore.DocumentSnapshot
import org.plzhelpus.smartcabinet_android.GROUP_NAME
import org.plzhelpus.smartcabinet_android.GROUP_REF

/**
 * 속한 그룹 목록을 관리하는 RecyclerView를 갱신해주는 DiffUtil.Callback
 */
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
        return (mOldDocuments[oldItemPosition].get(GROUP_NAME) == mNewDocuments[newItemPosition].get(GROUP_NAME)) &&
                (mOldDocuments[oldItemPosition].get(GROUP_REF) == mNewDocuments[newItemPosition].get(GROUP_REF))
    }
}
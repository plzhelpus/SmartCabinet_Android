package org.plzhelpus.smartcabinet_android.groupInfo.cabinet

import android.support.v7.util.DiffUtil
import com.google.firebase.firestore.DocumentSnapshot
import org.plzhelpus.smartcabinet_android.CABINET_REF
import org.plzhelpus.smartcabinet_android.DESCRIPTION

/**
 * 사물함 목록을 관리하는 RecyclerView를 갱신해주는 DiffUtil.Callback
 */
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
        return (mOldDocuments[oldItemPosition].get(DESCRIPTION) == mNewDocuments[newItemPosition].get(DESCRIPTION) &&
                (mOldDocuments[oldItemPosition].get(CABINET_REF) == mNewDocuments[newItemPosition].get(CABINET_REF)))
    }
}
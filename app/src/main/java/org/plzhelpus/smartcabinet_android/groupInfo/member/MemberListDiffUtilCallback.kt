package org.plzhelpus.smartcabinet_android.groupInfo.member

import android.support.v7.util.DiffUtil
import com.google.firebase.firestore.DocumentSnapshot
import org.plzhelpus.smartcabinet_android.EMAIL
import org.plzhelpus.smartcabinet_android.USER_REF

/**
 * 일반 회원 목록을 관리하는 RecyclerView를 갱신해주는 DiffUtil.Callback
 */

class MemberListDiffUtilCallback (
        private val mOldDocuments: List<DocumentSnapshot>,
        private val mNewDocuments : List<DocumentSnapshot>) : DiffUtil.Callback() {

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return mOldDocuments[oldItemPosition].reference == mNewDocuments[newItemPosition].reference
    }

    override fun getOldListSize(): Int {
        return mOldDocuments.size
    }

    override fun getNewListSize(): Int {
        return mNewDocuments.size
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return (mOldDocuments[oldItemPosition].get(EMAIL) == mNewDocuments[newItemPosition].get(EMAIL)) &&
                (mOldDocuments[oldItemPosition].get(USER_REF) == mNewDocuments[newItemPosition].get(USER_REF))
    }
}
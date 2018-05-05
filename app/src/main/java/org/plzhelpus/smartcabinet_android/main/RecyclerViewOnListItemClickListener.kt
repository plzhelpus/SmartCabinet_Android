package org.plzhelpus.smartcabinet_android.main

interface RecyclerViewOnListItemClickListener<T> {

    /**
     * 그룹 목록에서 한 그룹을 선택했을 때, 호출
     */
    fun onListItemClicked(item : T)
}
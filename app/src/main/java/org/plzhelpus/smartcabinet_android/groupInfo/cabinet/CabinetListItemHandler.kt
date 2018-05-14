package org.plzhelpus.smartcabinet_android.groupInfo.cabinet

interface CabinetListItemHandler<in T> {
    fun openOrCloseCabinet(item: T)
    fun deleteCabinet(item: T)
    fun editCabinetDescription(item: T)
}
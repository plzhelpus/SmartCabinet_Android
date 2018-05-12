package org.plzhelpus.smartcabinet_android.groupInfo.cabinet

interface CabinetListItemHandler<in T> {
    fun openCabinet(item: T)
    fun deleteCabinet(item: T)
    fun editCabinetDescription(item: T)
}
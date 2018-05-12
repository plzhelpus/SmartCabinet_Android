package org.plzhelpus.smartcabinet_android.groupInfo.admin

interface AdminListItemHandler<in T> {
    fun demoteAdminToMember(item: T)
    fun delegateOwnershipToAdmin(item: T)
    fun deleteAdmin(item: T)
}
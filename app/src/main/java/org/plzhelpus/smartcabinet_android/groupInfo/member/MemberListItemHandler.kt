package org.plzhelpus.smartcabinet_android.groupInfo.member

interface MemberListItemHandler<in T> {
    fun promoteMemberToAdmin(item: T)
    fun delegateOwnershipToMember(item: T)
    fun deleteMember(item: T)
}
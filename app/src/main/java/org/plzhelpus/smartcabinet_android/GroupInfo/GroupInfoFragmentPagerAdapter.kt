package org.plzhelpus.smartcabinet_android.groupInfo

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentReference
import org.plzhelpus.smartcabinet_android.R
import org.plzhelpus.smartcabinet_android.groupInfo.admin.AdminFragment
import org.plzhelpus.smartcabinet_android.groupInfo.cabinet.CabinetFragment
import org.plzhelpus.smartcabinet_android.groupInfo.member.MemberFragment
import java.lang.IllegalArgumentException

/**
 * Created by Donghwan Kim on 2018-03-26.
 *
 * 그룹 정보 안의 탭들을 관리하는 FragmentPagerAdapter
 */
class GroupInfoFragmentPagerAdapter(
        private var currentGroupDocumentReference: DocumentReference?,
        private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm){

    companion object {
        private val TAG = "GroupInfoPager"
    }

    private var cabinetFragment : CabinetFragment? = null
    private var adminFragment : AdminFragment? = null
    private var memberFragment : MemberFragment? = null

    private val tabTitleResId: Array<Int> = arrayOf(
            R.string.title_cabinet,
            R.string.title_admin,
            R.string.title_member
    )

    override fun getItem(position: Int): Fragment {
        Log.d(TAG, "pagerAdapter.getItem - $position")
        when(position){
            0 -> return CabinetFragment().apply {
                cabinetFragment = this
                mCurrentCabinetListReference = currentGroupDocumentReference?.collection("cabinet_ref")
            }
            1 -> return AdminFragment().apply {
                adminFragment = this
                mCurrentAdminListReference = currentGroupDocumentReference?.collection("admin_ref")
            }
            2 -> return MemberFragment().apply {
                memberFragment = this
                mCurrentMemberListReference = currentGroupDocumentReference?.collection("member_ref")
            }
            else -> throw IllegalArgumentException("Wrong position")
        }
    }

    override fun getCount(): Int {
        return tabTitleResId.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(tabTitleResId[position])
    }

    fun updateGroupInfo(newGroupDocumentReference: DocumentReference){
        Log.d(TAG, "update group info now")
        currentGroupDocumentReference = newGroupDocumentReference.apply {
            cabinetFragment?.apply { mCurrentCabinetListReference = collection("cabinet_ref") }
            adminFragment?.apply { mCurrentAdminListReference = collection("admin_ref") }
            memberFragment?.apply { mCurrentMemberListReference = collection("member_ref") }
        }
    }
}
package org.plzhelpus.smartcabinet_android.groupInfo

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log
import com.google.firebase.firestore.DocumentReference
import org.plzhelpus.smartcabinet_android.ADMIN_REF
import org.plzhelpus.smartcabinet_android.R
import org.plzhelpus.smartcabinet_android.groupInfo.admin.AdminFragment
import org.plzhelpus.smartcabinet_android.groupInfo.cabinet.CabinetFragment
import org.plzhelpus.smartcabinet_android.groupInfo.member.MemberFragment
import java.lang.IllegalArgumentException
import org.plzhelpus.smartcabinet_android.CABINET_REF
import org.plzhelpus.smartcabinet_android.MEMBER_REF

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

    private var mCabinetFragment : CabinetFragment? = null
    private var mAdminFragment : AdminFragment? = null
    private var mMemberFragment : MemberFragment? = null

    private val tabTitleResId: Array<Int> = arrayOf(
            R.string.title_cabinet,
            R.string.title_admin,
            R.string.title_member
    )

    override fun getItem(position: Int): Fragment {
        Log.d(TAG, "pagerAdapter.getItem - $position")
        when(position){
            0 -> return CabinetFragment().apply {
                mCabinetFragment = this
                mCurrentCabinetListReference = currentGroupDocumentReference?.collection(CABINET_REF)
            }
            1 -> return AdminFragment().apply {
                mAdminFragment = this
                mCurrentAdminListReference = currentGroupDocumentReference?.collection(ADMIN_REF)
            }
            2 -> return MemberFragment().apply {
                mMemberFragment = this
                mCurrentMemberListReference = currentGroupDocumentReference?.collection(MEMBER_REF)
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
            mCabinetFragment?.apply { mCurrentCabinetListReference = collection(CABINET_REF) }
            mAdminFragment?.apply { mCurrentAdminListReference = collection(ADMIN_REF) }
            mMemberFragment?.apply { mCurrentMemberListReference = collection(MEMBER_REF) }
        }
    }
}
package org.plzhelpus.smartcabinet_android.groupInfo

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import android.util.Log
import android.view.ViewGroup
import com.google.firebase.firestore.DocumentReference
import org.plzhelpus.smartcabinet_android.R
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

    private var currentFragment : Fragment? = null

    private val tabTitleResId: Array<Int> = arrayOf(
            R.string.title_cabinet,
            R.string.title_admin,
            R.string.title_member
    )

    override fun getItem(position: Int): Fragment =
        when(position){
            0 -> CabinetFragment().apply { mCurrentCabinetListReference = currentGroupDocumentReference?.collection("cabinet_refs") }
            1 -> AdminFragment().apply { mCurrentAdminListReference = currentGroupDocumentReference?.collection("admin_refs") }
            2 -> MemberFragment().apply { mCurrentMemberListReference = currentGroupDocumentReference?.collection("member_refs") }
            else -> throw IllegalArgumentException("Wrong position")
        }

    override fun setPrimaryItem(container: ViewGroup, position: Int, `object`: Any) {
        super.setPrimaryItem(container, position, `object`)
        currentFragment = `object` as Fragment
        Log.d(TAG, "new view pager position - $position")
    }

    override fun getCount(): Int {
        return tabTitleResId.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(tabTitleResId[position])
    }

    fun updateGroupInfo(newGroupDocumentReference: DocumentReference){
        currentFragment?.let {
            currentGroupDocumentReference = newGroupDocumentReference.apply {
                if(it is CabinetFragment) it.mCurrentCabinetListReference = collection("cabinet_ref")
                else if(it is AdminFragment)  it.mCurrentAdminListReference = collection("admin_ref")
                else if(it is MemberFragment)  it.mCurrentMemberListReference = collection("member_ref")
            }
        }
    }
}
package org.plzhelpus.smartcabinet_android.groupInfo

import android.content.Context
import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import org.plzhelpus.smartcabinet_android.R
import java.lang.IllegalArgumentException

/**
 * Created by Donghwan Kim on 2018-03-26.
 *
 * 그룹 정보 안의 탭들을 관리하는 FragmentPagerAdapter
 */
class GroupInfoFragmentPagerAdapter(private val context: Context, fm: FragmentManager) : FragmentPagerAdapter(fm){

    private val tabTitleResId: Array<Int> = arrayOf(
            R.string.title_cabinet,
            R.string.title_admin,
            R.string.title_member
    )

    override fun getItem(position: Int): Fragment =
        when(position){
            0 -> CabinetFragment()
            1 -> AdminFragment()
            2 -> MemberFragment()
            else -> throw IllegalArgumentException("Wrong position")
        }


    override fun getCount(): Int {
        return tabTitleResId.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        return context.getString(tabTitleResId[position])
    }
}
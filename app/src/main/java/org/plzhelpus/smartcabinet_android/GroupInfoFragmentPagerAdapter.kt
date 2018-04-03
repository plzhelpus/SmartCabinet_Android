package org.plzhelpus.smartcabinet_android

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import java.lang.IllegalArgumentException

/**
 * Created by Donghwan Kim on 2018-03-26.
 *
 * 그룹 정보 안의 탭들을 관리하는 FragmentPagerAdapter
 */
class GroupInfoFragmentPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm){
    val tabIconResId: Array<Int> = arrayOf(
            R.drawable.ic_lock_outline_white_18dp,
            R.drawable.ic_people_white_18dp
    )
    override fun getItem(position: Int): Fragment =
        when(position){
            0 -> CabinetFragment()
            1 -> MemberFragment()
            else -> throw IllegalArgumentException("Wrong position")
        }


    override fun getCount(): Int {
        return tabIconResId.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        // 타이틀을 보여주지 않음
        return null
    }
}
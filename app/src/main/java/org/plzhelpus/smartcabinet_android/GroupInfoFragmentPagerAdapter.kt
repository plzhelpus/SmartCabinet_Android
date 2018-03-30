package org.plzhelpus.smartcabinet_android

import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.support.v4.app.FragmentPagerAdapter
import java.lang.IllegalArgumentException

/**
 * Created by Donghwan Kim on 2018-03-26.
 *
 *
 */
class GroupInfoFragmentPagerAdapter(fm: FragmentManager) : FragmentPagerAdapter(fm){
    val numItems: Int = 2

    override fun getItem(position: Int): Fragment =
        when(position){
            0 -> CabinetFragment.newInstance(1)
            1 -> MemberFragment.newInstance(1)
            else -> throw IllegalArgumentException("Wrong position")
        }


    override fun getCount(): Int {
        return numItems
    }
}
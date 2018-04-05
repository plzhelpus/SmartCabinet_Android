package org.plzhelpus.smartcabinet_android.Cabinet

import android.util.Log
import android.view.View

/**
 * Created by Donghwan Kim on 2018-03-23.
 *
 * 사물함에 열림/닫힘 등의 요청을 할 때 누르는 버튼의 클릭 이벤트를 처리하는 OnClickListener
*/

class CabinetRequestOnClickListener : View.OnClickListener{
    private val TAG = "CabinetRequestButton"

    override fun onClick(v: View?) {
        Log.d(TAG, "Cabinet request button clicked")
    }
}
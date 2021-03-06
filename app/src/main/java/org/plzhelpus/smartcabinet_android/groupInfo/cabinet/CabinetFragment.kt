package org.plzhelpus.smartcabinet_android.groupInfo.cabinet

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.cabinet_list.*
import org.plzhelpus.smartcabinet_android.R
import org.plzhelpus.smartcabinet_android.main.MainActivity

/**
 * 그룹 내의 사물함 목록을 보여주는 프래그먼트
 */
class CabinetFragment : Fragment() {

    companion object {
        private var TAG = "CabinetFragment"
    }

    var mCurrentCabinetListReference : CollectionReference? = null
        set(value) {
            field = value
            registerCabinetListListener()
        }
    private var mListenerRegistration : ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.cabinet_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            view.run{
                layoutManager = LinearLayoutManager(context)
                adapter = CabinetRecyclerViewAdapter(ArrayList(), activity as CabinetListItemHandler<DocumentSnapshot>)
                setHasFixedSize(true)
            }
        }
        return view
    }

    private fun registerCabinetListListener() {
        // 해당 뷰가 존재하지 않으면 실행하면 안됨.
        if(cabinet_list == null) return
        mCurrentCabinetListReference?.let{ currentCabinetListReference ->
            mListenerRegistration?.remove()
            mListenerRegistration = currentCabinetListReference.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let{exception ->
                    Log.w(TAG, "Cabinet list - Listen failed.", exception)
                    // TODO 테스트 필요
                    (activity as MainActivity).showSnackbar(R.string.cannot_load_list)
                    return@addSnapshotListener
                }

                querySnapshot?.run{
                    Log.d(TAG, "Cabinet list found")
                    (cabinet_list?.adapter as CabinetRecyclerViewAdapter?)?.updateList(documents)
                }?.let{
                    Log.d(TAG, "Cabinet list null")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerCabinetListListener()
    }

    override fun onPause() {
        super.onPause()
        mListenerRegistration?.remove()
    }
}

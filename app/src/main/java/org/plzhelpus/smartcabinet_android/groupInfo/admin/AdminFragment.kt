package org.plzhelpus.smartcabinet_android.groupInfo.admin

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
import kotlinx.android.synthetic.main.admin_list.*
import org.plzhelpus.smartcabinet_android.R
import org.plzhelpus.smartcabinet_android.main.MainActivity

/**
 * 그룹 관리자 목록을 보여주는 프래그먼트
 */
class AdminFragment : Fragment() {

    companion object {
        private var TAG = "AdminFragment"
    }

    var mCurrentAdminListReference : CollectionReference? = null
        set(value) {
            field = value
            registerAdminListListener()
        }
    private var mListenerRegistration : ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.admin_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            view.run {
                layoutManager = LinearLayoutManager(context)
                adapter = AdminRecyclerViewAdapter(ArrayList(), activity as AdminListItemHandler<DocumentSnapshot>)
                setHasFixedSize(true)
            }
        }
        return view
    }

    private fun registerAdminListListener() {
        // 해당 뷰가 존재하지 않으면 실행하면 안됨.
        if(admin_list == null) return
        mCurrentAdminListReference?.let{ currentAdminListReference ->
            mListenerRegistration?.remove()
            mListenerRegistration = currentAdminListReference.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let { exception ->
                    Log.w(TAG, "Admin list - Listen failed.", exception)
                    // TODO 테스트 필요
                    (activity as MainActivity).showSnackbar(R.string.cannot_load_list)
                    return@addSnapshotListener
                }

                querySnapshot?.run{
                    Log.d(TAG, "Admin list found")
                    (admin_list?.adapter as AdminRecyclerViewAdapter?)?.updateList(documents)
                }?.let{
                    Log.d(TAG, "Admin list null")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerAdminListListener()
    }

    override fun onPause() {
        super.onPause()
        mListenerRegistration?.remove()
    }
}

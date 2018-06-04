package org.plzhelpus.smartcabinet_android.groupInfo.member

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
import kotlinx.android.synthetic.main.member_list.*
import org.plzhelpus.smartcabinet_android.R
import org.plzhelpus.smartcabinet_android.main.MainActivity

/**
 * 그룹 일반 회원 목록을 보여주는 프래그먼트
 */
class MemberFragment : Fragment(){

    companion object {
        private var TAG = "MemberFragment"
    }

    var mCurrentMemberListReference : CollectionReference? = null
        set(value) {
            field = value
            registerMemberListListener()
        }
    private var mListenerRegistration : ListenerRegistration? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.member_list, container, false)

        // Set the adapter
        if (view is RecyclerView) {
            view.run {
                layoutManager = LinearLayoutManager(context)
                adapter = MemberRecyclerViewAdapter(ArrayList(), activity as MemberListItemHandler<DocumentSnapshot>)
                setHasFixedSize(true)
            }
        }
        return view
    }

    private fun registerMemberListListener() {
        // 해당 뷰가 존재하지 않으면 실행하면 안됨.
        if(member_list == null) return
        mCurrentMemberListReference?.let{ currentMemberListReference ->
            mListenerRegistration?.remove()
            mListenerRegistration = currentMemberListReference.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.let{exception ->
                    Log.w(TAG, "Member list - Listen failed.", exception)
                    // TODO 테스트 필요
                    (activity as MainActivity).showSnackbar(R.string.cannot_load_list)
                    return@addSnapshotListener
                }

                querySnapshot?.run{
                    Log.d(TAG, "Member list found")
                    (member_list?.adapter as MemberRecyclerViewAdapter).updateList(documents)
                }?.let{
                    Log.d(TAG, "Member list null")
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        registerMemberListListener()

    }

    override fun onPause() {
        super.onPause()
        mListenerRegistration?.remove()
    }
}

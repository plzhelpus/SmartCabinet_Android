package org.plzhelpus.smartcabinet_android.groupInfo

import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.android.synthetic.main.admin_list.*
import org.plzhelpus.smartcabinet_android.R

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [AdminFragment.OnListFragmentInteractionListener] interface.
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
                adapter = AdminRecyclerViewAdapter(ArrayList())
                setHasFixedSize(true)
            }
        }
        return view
    }

    private fun registerAdminListListener() {
        mCurrentAdminListReference?.let{ currentAdminListReference ->
            mListenerRegistration?.remove()
            mListenerRegistration = currentAdminListReference.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.run{
                    Log.w(TAG, "Admin list - Listen failed.", this)
                    return@addSnapshotListener
                }

                querySnapshot?.run{
                    Log.d(TAG, "Admin list found")
                    (admin_list.adapter as AdminRecyclerViewAdapter).updateList(documents)
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

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
import kotlinx.android.synthetic.main.cabinet_list.*
import org.plzhelpus.smartcabinet_android.R

/**
 * A fragment representing a list of Items.
 *
 *
 * Activities containing this fragment MUST implement the [OnListFragmentInteractionListener]
 * interface.
 */
/**
 * Mandatory empty constructor for the fragment manager to instantiate the
 * fragment (e.g. upon screen orientation changes).
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
                adapter = CabinetRecyclerViewAdapter(ArrayList())
                setHasFixedSize(true)
            }
        }
        return view
    }

    private fun registerCabinetListListener() {
        mCurrentCabinetListReference?.let{ currentCabinetListReference ->
            mListenerRegistration?.remove()
            mListenerRegistration = currentCabinetListReference.addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                firebaseFirestoreException?.run{
                    Log.w(TAG, "Cabinet list - Listen failed.", this)
                    return@addSnapshotListener
                }

                querySnapshot?.run{
                    Log.d(TAG, "Cabinet list found")
                    (cabinet_list.adapter as CabinetRecyclerViewAdapter).updateList(documents)
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

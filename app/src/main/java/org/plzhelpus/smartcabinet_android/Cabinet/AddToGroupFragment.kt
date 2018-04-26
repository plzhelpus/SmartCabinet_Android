package org.plzhelpus.smartcabinet_android.cabinet


import android.os.Bundle
import android.support.v4.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_add_to_group.*
import org.plzhelpus.smartcabinet_android.R

/**
 * A simple [Fragment] subclass.
 *
 */
class AddToGroupFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_add_to_group, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        add_to_group_submit_button.setOnClickListener {

        }
    }
}

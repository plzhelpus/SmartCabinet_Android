package org.plzhelpus.smartcabinet_android.Cabinet

import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import kotlinx.android.synthetic.main.activity_new_cabinet.*
import org.plzhelpus.smartcabinet_android.GroupInfo.CabinetFragment
import org.plzhelpus.smartcabinet_android.R

class NewCabinetActivity : AppCompatActivity(), NewCabinetMenuFragment.NewCabinetMenuListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_new_cabinet)

        var fragmentTransaction = supportFragmentManager.beginTransaction()
                .add(R.id.fragment_container, NewCabinetMenuFragment())
                .commit();
    }

    override fun onAddButtonClicked() {
        var fragmentTransaction = supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, AddToGroupFragment())
                .addToBackStack(null)
                .commit();
    }

    override fun onCreateButtonClicked() {
        var fragmentTransaction = supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, CreateGroupFragment())
                .addToBackStack(null)
                .commit();
    }
}

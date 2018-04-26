package org.plzhelpus.smartcabinet_android.cabinet

import android.content.Context
import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import org.plzhelpus.smartcabinet_android.R

class NewCabinetActivity : AppCompatActivity(), NewCabinetMenuFragment.NewCabinetMenuListener {

    companion object {
        fun createIntent(context: Context): Intent{
            return Intent(context, NewCabinetActivity::class.java)
        }
    }

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

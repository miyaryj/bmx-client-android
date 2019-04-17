package com.sony.android.bmxclient

import android.net.Uri
import android.os.Bundle
import android.support.v7.app.AppCompatActivity

class MainActivity : AppCompatActivity(), ImageLabelFragment.OnFragmentInteractionListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragment = ImageLabelFragment.newInstance("param1","param2")
        val transaction = supportFragmentManager.beginTransaction()
        transaction.add(R.id.fragmentContainer, fragment)
        transaction.commit()
    }

    override fun onFragmentInteraction(uri: Uri) {

    }
}

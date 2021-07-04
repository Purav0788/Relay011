package com.example.relay

import android.app.Application
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class myApp : Application() {
    /*private lateinit var auth: FirebaseAuth*/

    @Override
    override fun onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        //could not start an activity from here, its not allowed
    }
}
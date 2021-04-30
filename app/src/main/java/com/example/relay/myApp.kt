package com.example.relay

import android.app.Application
import com.google.firebase.database.FirebaseDatabase

class myApp : Application() {
    @Override
    override fun onCreate() {
        super.onCreate();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
    }
}
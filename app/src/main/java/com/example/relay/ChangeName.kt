package com.example.relay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_change_name.*
import java.time.LocalDateTime

class ChangeName : AppCompatActivity() {
    private lateinit var user1:String
    lateinit var reference1: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_name)
        user1 = intent.getStringExtra("user1")!!
        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/users/$user1")
    }

    public fun changeName(view:View){
        reference1.child("name").setValue(yourName.text.toString())
        sendChangedUserNameBroadCast()
        finish()
    }

    private fun sendChangedUserNameBroadCast(){
        Log.d("receiverinChat","hello")
        val intent = Intent("changedUserName")
        intent.putExtra("newName", yourName.text.toString())
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }
}
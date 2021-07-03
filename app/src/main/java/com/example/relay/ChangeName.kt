package com.example.relay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.database.*
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
        setUpName(user1)
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

    private fun setUpName(phoneNumber:String){
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query = reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
                        var yourName = findViewById(R.id.yourName) as EditText
                        yourName.setText(user.child("name").value.toString())
                    }
                }
                else{
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}
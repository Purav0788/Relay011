package com.example.relay

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_change_business_name.*
import kotlinx.android.synthetic.main.activity_change_name.*
import kotlinx.android.synthetic.main.activity_home_screen.*
import java.time.LocalDateTime

class ChangeBusinessName : AppCompatActivity() {
    private lateinit var user1:String
    lateinit var reference1: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_business_name)
        user1 = intent.getStringExtra("user1")!!
        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/users/$user1")

        setUpBusinessName(user1)
    }
    public fun changeBusinessName(view: View){
        reference1.child("business_name").setValue(yourBusinessName.text.toString())
        sendChangedBusinessNameBroadCast()
        finish()
    }

    private fun sendChangedBusinessNameBroadCast(){
        Log.d("receiverinChat","hello")
        val intent = Intent("changedBusinessName")
        intent.putExtra("newBusinessName", yourBusinessName.text.toString())
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

    private fun setUpBusinessName(phoneNumber:String){
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query = reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
                        var yourBusinessName = findViewById(R.id.yourBusinessName) as EditText
                        yourBusinessName.setText(user.child("business_name").value.toString())
                    }
                }
                else{
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
}
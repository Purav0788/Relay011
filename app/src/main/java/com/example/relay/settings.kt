package com.example.relay

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home_screen.*


class settings : AppCompatActivity() {
    private lateinit var user1:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        user1 = intent.getStringExtra("user1")!!
        findUserInDb(user1)
    }

    private fun findUserInDb(phoneNumber: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query = reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
                        refreshData(user)
                    }
                }
                else{
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }


    private fun refreshData(myUser:DataSnapshot){
        _yourBusinessName.text = myUser.child("business_name").value.toString()
        _yourName.text = myUser.child("name").value.toString()
        _yourPhoneNumber.text = myUser.child("phone_number").value.toString()
    }

    public fun logOut(view:View){
        finishAffinity()
        val intent = Intent(applicationContext, LoginByNumber::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

}
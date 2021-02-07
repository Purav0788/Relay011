package com.example.relay

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.lang.reflect.Field


class homeScreen : AppCompatActivity() {
    // it comes after loginbyNumber or entering customer Details,
    // for customer details

    //query the phone for data if already signed up
    //reload customer details from server or phone storage


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_screen)
        val mobile = intent.getStringExtra("phoneNumber")
        val user = FirebaseAuth.getInstance().currentUser

        val fb = FirebaseDatabase.getInstance().reference
        val users = fb.child("relay-28f2e-default-rtdb/users")
        val query1 = users.orderByChild("phone_number").equalTo(mobile)

        FirebaseAuth.getInstance().signOut()


//        ref.addListenerForSingleValueEvent(object : ValueEventListener {
//            override fun onDataChange(dataSnapshot: DataSnapshot) {
//                val tempProfile = User() //this is my user_class Class
//                val fields: Array<Field> = tempProfile.getClass().getDeclaredFields()
//                for (field in fields) {
//                    Log.i(
//                        FragmentActivity.TAG,
//                        field.getName().toString() + ": " + dataSnapshot.child(field.getName())
//                            .value
//                    )
//                }
//            }
//
//            override fun onCancelled(databaseError: DatabaseError) {}
//        })



    }




}
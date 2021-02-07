package com.example.relay

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_customer_details.*


class getCustomerDetails : AppCompatActivity() {
    //
    private var mobile:String? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_details)
        mobile = intent.getStringExtra("phoneNumber")
        Log.d("mobile","$mobile.toString()")
        FirebaseAuth.getInstance().signOut()
    }
    public fun storeInDB(view:View){
        val businessName = enterBusinessName.text.toString()
        val yourName = enterYourName.text.toString()
//        val usersTable =
        val user = FirebaseAuth.getInstance().currentUser

        val fb = FirebaseDatabase.getInstance().reference
        val table = fb.child("relay-28f2e-default-rtdb/users")
        val newCustomer = table.push()
        newCustomer.child("business_name").setValue(businessName)
        newCustomer.child("name").setValue(yourName)
        newCustomer.child("phone_number").setValue(mobile)
//        val ref = FirebaseDatabase.getInstance().getReference("relay-28f2e-default-rtdb/users")
//        ref.child(user!!.uid).setValue()
    }
}
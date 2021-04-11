package com.example.relay

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_customer_details.*


class getCustomerDetails : AppCompatActivity() {

    private lateinit var mobile:String
    private lateinit var fb: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_customer_details)
        mobile = intent.getStringExtra("phoneNumber")!!
        fb = FirebaseDatabase.getInstance().reference
    }
    public fun storeInDB(view:View){
        val businessName = enterBusinessName.text.toString()
        val yourName = enterYourName.text.toString()
        makeUserInDb(businessName, yourName)
        makeUser1InChat(mobile)

        //create an intent and send
        val intent = Intent(this@getCustomerDetails, homeScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("phoneNumber", mobile)
        startActivity(intent)
    }

    private fun makeUserInDb(businessName:String, userName:String){
        val user = FirebaseAuth.getInstance().currentUser
        val newCustomer  = fb.child("users").child(mobile)
        newCustomer.child("business_name").setValue(businessName)
        newCustomer.child("name").setValue(userName)
        newCustomer.child("phone_number").setValue(mobile)
    }

    public fun makeUser1InChat(user1:String){
        val reference = FirebaseDatabase.getInstance().reference
        val chatRef = reference.child("chats")
        val map: MutableMap<String, String> = HashMap()
        map["username"] = ""
        map["time"] = "0"
        chatRef.child("$user1").setValue(" ").addOnSuccessListener {
            //making empty user initially
            chatRef.child("$user1").push().setValue(map).addOnSuccessListener {  }
        }
    }
}
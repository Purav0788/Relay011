package com.example.relay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_change_business_name.*
import kotlinx.android.synthetic.main.activity_change_name.*

class changeBusinessName : AppCompatActivity() {
    private lateinit var user1:String
    lateinit var reference1: DatabaseReference
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_change_business_name)
        user1 = intent.getStringExtra("user1")!!
        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/users/$user1")
    }
    public fun changeBusinessName(view: View){
        reference1.child("business_name").setValue(yourBusinessName.text.toString())
        finish()
    }
}
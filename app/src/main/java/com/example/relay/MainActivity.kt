package com.example.relay

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // Initialize Firebase Auth
        val intent:Intent = Intent(this, getCustomerDetails::class.java)

        bunchOfButtons.setOnItemClickListener{_,_,_,_->

            //either already logged in, so this will send to home screen
            // val myIntent
            val user = FirebaseAuth.getInstance().currentUser

            if(user!= null) {
                //already signed in
                val myIntent = Intent(this,LoginByNumber::class.java)
                startActivity(myIntent)
            }
           else{
                //go to signup or signin
            val myIntent = Intent(this, LoginByNumber::class.java)
            startActivity(myIntent)
        }
        }
    }
}
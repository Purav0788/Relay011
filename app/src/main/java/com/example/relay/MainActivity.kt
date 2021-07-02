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
        /*setContentView(R.layout.activity_main)*/
        // Initialize Firebase Auth
//        val intent: Intent = Intent(this, getCustomerDetails::class.java)

        //either already logged in, so this will send to home screen
        // val myIntent
        val user = FirebaseAuth.getInstance().currentUser
        //he cant log out in the first place if he has not been to settings screen or passed the get customer details, so
        //i dont need to handle the case that the user maynot have given those details to us
        if (user != null) {
            //already signed in
            val myIntent = Intent(this, homeScreen::class.java)
            val user1:String = user.phoneNumber!!

            var number = user1
//                originalAddContactNumber = number1
            number = number.replace("[^0-9]", "")
            //cutting the extra 91 or any 0 prefix from this number
            number = number.filter { !it.isWhitespace() }
            var extraPrefix: Int = number.length - 10
            number = number.drop(extraPrefix)


            myIntent.putExtra("user1", number)
            startActivity(myIntent)
        } else {
            //go to signup or signin
            val myIntent = Intent(this, LoginByNumber::class.java)
            startActivity(myIntent)
        }

        /*bunchOfButtons.setOnItemClickListener { _, _, _, _ ->

            //either already logged in, so this will send to home screen
            // val myIntent
            val user = FirebaseAuth.getInstance().currentUser
            //he cant log out in the first place if he has not been to settings screen or passed the get customer details, so
            //i dont need to handle the case that the user maynot have given those details to us
            if (user != null) {
                //already signed in
                val myIntent = Intent(this, homeScreen::class.java)
                val user1:String = user.phoneNumber!!

                var number = user1
//                originalAddContactNumber = number1
                number = number.replace("[^0-9]", "")
                //cutting the extra 91 or any 0 prefix from this number
                number = number.filter { !it.isWhitespace() }
                var extraPrefix: Int = number.length - 10
                number = number.drop(extraPrefix)


                myIntent.putExtra("user1", number)
                startActivity(myIntent)
            } else {
                //go to signup or signin
                val myIntent = Intent(this, LoginByNumber::class.java)
                startActivity(myIntent)
            }
        }*/
    }
}

package com.example.relay

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_login_by_number.*


class LoginByNumber : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login_by_number)
    }
    public fun verifyPhone(myView: View){

        val mobile: String = userNumber.text.toString().trim()

        if(mobile.isEmpty() || mobile.length < 10){
            userNumber.setError("Enter a valid mobile")
            userNumber.requestFocus()
            return
        }

        val myIntent = Intent(this, verifyPhoneNumber::class.java)

        myIntent.putExtra("mobile", mobile)
        startActivity(myIntent)
    }
}
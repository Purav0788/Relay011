package com.example.relay

//import android.R

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.firebase.FirebaseApp
import com.google.firebase.FirebaseException
import com.google.firebase.auth.*
import com.google.firebase.auth.PhoneAuthProvider.ForceResendingToken
import com.google.firebase.auth.PhoneAuthProvider.OnVerificationStateChangedCallbacks
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_verify_phone.*
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit


class verifyPhoneNumber : AppCompatActivity() {
    //firebase token
    private var mVerificationId: String? = null
    //firebase auth object
    private var mAuth: FirebaseAuth? = null
    //initializing db
    var fb: DatabaseReference? = null
    var mobile:String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_verify_phone)
        FirebaseApp.initializeApp(this@verifyPhoneNumber)
        fb = FirebaseDatabase.getInstance().reference
        mAuth = FirebaseAuth.getInstance()
        mobile = intent.getStringExtra("mobile")
        sendVerificationCode(mobile!!)
        Log.d("verify", "I am here")

    }

    public fun submitOtp(myview: View){
    var otpUserEntered :String = otpNumber.text.toString().trim()
        if (otpUserEntered.isEmpty() || otpUserEntered.length < 6) {
            otpNumber.setError("Enter valid code");
            otpNumber.requestFocus();
        }
        verifyVerificationCode(otpUserEntered);
    }

    private fun sendVerificationCode(mobile: String) {
        val options = PhoneAuthOptions.newBuilder(mAuth!!)
            .setPhoneNumber("+91" + mobile) // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(this) // Activity (for callback binding)
            .setCallbacks(mCallbacks) // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)
    }

    private val mCallbacks: OnVerificationStateChangedCallbacks =
        object : OnVerificationStateChangedCallbacks() {
            override fun onVerificationCompleted(phoneAuthCredential: PhoneAuthCredential) {

                //Getting the code sent by SMS
                val otpDetected = phoneAuthCredential.smsCode

                //sometime the code is not detected automatically
                //in this case the code will be null
                //so user has to manually enter the code
                if (otpDetected != null) {
                    otpNumber.setText(otpDetected)
                    //verifying the code
                    verifyVerificationCode(otpDetected)
                }
            }

            override fun onVerificationFailed(e: FirebaseException) {
                Toast.makeText(this@verifyPhoneNumber, e.message, Toast.LENGTH_LONG).show()
            }

            override fun onCodeSent(
                s: String,
                forceResendingToken: ForceResendingToken
            ) {
                super.onCodeSent(s, forceResendingToken)

                //storing the verification id that is sent to the user
                mVerificationId = s
            }
        }

    private fun verifyVerificationCode(code: String) {
        //creating the credential
        val credential = PhoneAuthProvider.getCredential(mVerificationId!!, code)

        //signing the user
        signInWithPhoneAuthCredential(credential)
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        mAuth!!.signInWithCredential(credential)
            .addOnCompleteListener(
                this@verifyPhoneNumber,
                OnCompleteListener<AuthResult?> { task ->
                    if (task.isSuccessful) {
                        //verification successful we will start the profile activity
                       isUserInDb(mobile!!)
                    } else {
                        Toast.makeText(this@verifyPhoneNumber, "doesnt work", Toast.LENGTH_LONG).show()
                    }
                })
    }

    private fun isSignedIn(): Boolean {
        return mAuth!!.currentUser != null
    }

    private fun isUserInDb(phoneNumber: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query = reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
            query.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                        Log.d("verify", "inside if datasnapshot")
                        goToHomeScreen()
                    }
                    else{
                        Log.d("verify","inside else datasnapshot")
                        goToGetCustomerDetails()
                    }
                }
                override fun onCancelled(databaseError: DatabaseError) {}
            })
    }

    private fun goToHomeScreen(){
        //if user in db then send to homescreen
        Log.d("verify","I am in if")
        val intent = Intent(this@verifyPhoneNumber, homeScreen::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        intent.putExtra("user1", mobile)
        startActivity(intent)
    }

    private fun goToGetCustomerDetails(){
        //if user not in db then signingup send to getCustomerDetails
        Log.d("verify","I am in else")
        val intent = Intent(this@verifyPhoneNumber, getCustomerDetails::class.java)
        intent.putExtra("phoneNumber", mobile)
        startActivity(intent)
    }
}
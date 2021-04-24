package com.example.relay

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_home_screen.*


class settings : AppCompatActivity() {
    private val businessNameChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var businessName = intent!!.getStringExtra("newBusinessName")!!
            _yourBusinessName.setText(businessName)
        }
    }

    private val userNameChangeReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            var userName = intent!!.getStringExtra("newName")!!
            _yourName.setText(userName)
        }
    }
    private lateinit var user1:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        user1 = intent.getStringExtra("user1")!!
        findUserInDbAndRefreshData(user1)
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(businessNameChangeReceiver, IntentFilter("changedBusinessName"))
        LocalBroadcastManager.getInstance(this)
            .registerReceiver(userNameChangeReceiver, IntentFilter("changedUserName"))
    }
//    override fun onResume() {
//        super.onResume()
////        findUserInDbAndRefreshData(user1)
//    }

    override fun onDestroy() {
        super.onDestroy()
        LocalBroadcastManager.getInstance(this).unregisterReceiver(userNameChangeReceiver);
        LocalBroadcastManager.getInstance(this).unregisterReceiver(businessNameChangeReceiver);
    }

    private fun findUserInDbAndRefreshData(phoneNumber: String) {
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
        FirebaseAuth.getInstance().signOut();
        startActivity(intent)
    }

    public fun changeBusinessName(view:View){
        val intent = Intent(this@settings, changeBusinessName::class.java)
        intent.putExtra("user1", user1)
        startActivity(intent);
    }
    public fun changeName(view:View){
        val intent = Intent(this@settings, changeName::class.java)
        intent.putExtra("user1", user1)
        startActivity(intent)
    }

}
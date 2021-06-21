package com.example.relay

import android.app.Activity
import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.my_orders_list_item.*
import kotlinx.android.synthetic.main.order_confirm_list_item.view.*
import java.util.*
import kotlin.collections.HashMap

class orderReceived0 : AppCompatActivity() {
    private lateinit var orderID:String
    lateinit var reference1: DatabaseReference;
    private lateinit var user1:String
    private lateinit var user2:String
    private var LAUNCH_ORDER_RECEIVED:Int = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderID = intent.getStringExtra("orderID")!!
        user1 = intent.getStringExtra("user1")!!
        user2 = intent.getStringExtra("user2")!!
        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/orders/$orderID"
        )
        reference1.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var isOrderCancelled = snapshot.child("orderCancelled").value.toString()
                var isOrderConfirmed = snapshot.child("orderConfirmed").value.toString()
                lateinit var orderStatus:String
                if(isOrderCancelled == "true"){
                    orderStatus = "orderCancelled"
                }else if(isOrderConfirmed == "true"){
                    orderStatus = "orderConfirmed"
                }else{
                    orderStatus = "orderSent"
                }
                if(orderStatus == "orderCancelled"){
                    renderOrderReceivedAndCancelled(snapshot)
                }else if (orderStatus == "orderConfirmed"){
                    renderOrderReceivedAndConfirmed(snapshot)
                }else{
                    renderOrderReceived(snapshot)
                }
            }

        })

    }

    private fun renderOrderReceived(snapshot:DataSnapshot){
        setContentView(R.layout.activity_order_received0)
        setUpCommonData(snapshot)
    }
    private fun renderOrderReceivedAndConfirmed(snapshot:DataSnapshot){
        setContentView(R.layout.activity_order_received0_and_confirmed)
        setUpCommonData(snapshot)
        setUpConfirmedDialogBox()
    }

    private fun setUpConfirmedDialogBox() {
        val textView = findViewById(R.id._confirmed) as TextView
        textView.text = "This order was confirmed"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun cancelOrder(view:View){
        myHelper.sendOrderCancellationMessage(orderID,user1, user2)
        finish()
    }

    private fun renderOrderReceivedAndCancelled(snapshot:DataSnapshot){
        setContentView(R.layout.activity_order_received0_and_cancelled)
        setUpCommonData(snapshot)
        setUpCancelledDialogBox()
    }

    private fun setUpCancelledDialogBox() {
        val query:Query = reference1
        query.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var myOrderMap = snapshot.value as HashMap<String, Any>
                    /*       Log.d("myOrderMap", myOrderMap.toString())
                           if (myOrderMap["orderCancelled"] == "true") {*/
                    setUpCancellerName(myOrderMap["orderCancelledBy"].toString())
                    /*} else {
                        *//*Toast.makeText(this@orderSent, "order is already cancelled once!",
                            Toast.LENGTH_LONG).show();*//*
                    }*/
                }
            }
        })
    }

    private fun setUpCommonData(snapshot:DataSnapshot){
        var deliveryDate = snapshot.child("deliveryDate").value
        var notes = snapshot.child("notes").value
        var address = snapshot.child("address").value
        var orderList: DataSnapshot = snapshot.child("orderList")
        val textView = findViewById(R.id.deliveryDate) as TextView
        textView.text= deliveryDate.toString()
        val textView1 = findViewById(R.id.address) as TextView
        textView1.text = address.toString()
        val textView2 = findViewById(R.id.notes) as TextView
        textView2.text = notes.toString()
        val delimiter = "#"
        var orders = ""
        var count = 0;
        for ( i in orderList.children){

            if ( i!= null) {
                var t = i.value as String
                count++;
                val splitString = t.split(delimiter).toTypedArray();
                orders+=splitString[0]+" "+splitString[1]+" "+splitString[2]
                orders += '\n';
            }
        }
        val textView3 = findViewById(R.id.order) as TextView
        textView3.text = orders
        val textView4 = findViewById(R.id.items) as TextView
        textView4.text = count.toString()

        setUpBuyerName(snapshot.child("orderFrom").value.toString())
        setUpOrderID()
    }

    private fun setUpBuyerName(phoneNumber:String) {
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query =
            reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
                        val textView = findViewById(R.id._sellerName) as TextView
                        textView.text = user.child("business_name").value as String
                    }
                } else {
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun setUpOrderID() {
        var fakeOrderID = orderID.subSequence(1,6)
        val textView = findViewById(R.id._orderID) as TextView
        textView.text = fakeOrderID.toString()
    }

    private fun setUpCancellerName(cancellerNumber:String){
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query =
            reference.child("users").orderByChild("phone_number").equalTo(cancellerNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
                        val textView = findViewById(R.id._cancellerName) as TextView
                        val cancellerName = user.child("business_name").value as String
                        textView.text = "This Order Was Cancelled By$cancellerName"
                    }
                } else {
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LAUNCH_ORDER_RECEIVED) {
            if (resultCode == Activity.RESULT_OK) {
                finish()
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }
    public fun quotePrice(view: View){
        val intent = Intent(this@orderReceived0, orderReceived1::class.java)
        intent.putExtra("orderID", orderID)
        intent.putExtra("user1",user1)
        intent.putExtra("user2",user2)
        startActivityForResult(intent, LAUNCH_ORDER_RECEIVED)
    }
}
package com.example.relay

import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import com.google.firebase.database.*
import java.time.LocalDateTime
import kotlin.collections.HashMap


object myHelper {

    @RequiresApi(Build.VERSION_CODES.O)
    public fun sendOrderCancellationMessage(orderID: String, user1: String, user2: String) {

        //checking if the order is already cancelled
        var reference = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/orders/" + orderID
        )
        val query:Query = reference
        query.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var myOrderMap = snapshot.value as HashMap<String, Any>
                    Log.d("myOrderMap", myOrderMap.toString())
                    if (myOrderMap["orderCancelled"] == "false") {
                        sendOrderCancellationMessage2(orderID, user1, user2)
                    } else {
                        return
                    }
                }
            }
        })

    }

    fun updateLastMessages(
        user2: String,
        time: LocalDateTime,
        lastMessage: String,
        user1: String
    ) {
        var myReference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/chats/" + user2
        )
        var myReference2 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/chats/" + user1
        )

        val map1: MutableMap<String, Any> = HashMap()
        map1["sentByUser"] = user1
        map1["time"] = time
        map1["lastSentOrReceivedMessage"] = lastMessage

        val reference = FirebaseDatabase.getInstance().reference
        val query: Query =
            reference.child("users").orderByChild("phone_number").equalTo(user1)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        val user1Name = user.child("name").value as String
                        map1["sentByUserName"] = user1Name
                        myReference1.child(user1).child("lastMessageDetails").updateChildren(map1)
                        myReference2.child(user2).child("lastMessageDetails").updateChildren(map1)
                    }
                } else {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    public fun setOrderStatusCancelled(orderID:String, user1:String) {
        var orderCancelled: String = "true"
        val myMap: MutableMap<String, Any> = HashMap()
        var reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/orders/" + orderID
        )
        myMap["orderCancelled"] = orderCancelled
        myMap["orderCancelledBy"] = user1
        val query:Query = reference1
        query.addListenerForSingleValueEvent(object:ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                if (snapshot.exists()) {
                    var myOrderMap = snapshot.value as HashMap<String, Any>
                    Log.d("myOrderMap", myOrderMap.toString())
                    if (myOrderMap["orderCancelled"] == "false") {
                        reference1.updateChildren(myMap)
                       /* val intent = Intent()
                        intent.putExtra("result", orderID)
                       *//* setResult(Activity.RESULT_OK, intent)
                        finish()*/
                    } else {
                        /*Toast.makeText(this@orderSent, "order is already cancelled once!",
                            Toast.LENGTH_LONG).show();*/
                    }
                }
            }
        })
    }
    @RequiresApi(Build.VERSION_CODES.O)
    public fun sendOrderCancellationMessage2(orderID: String, user1: String, user2: String) {
        val map: MutableMap<String, Any> = HashMap()
        val time = LocalDateTime.now()
        map["orderID"] = orderID
        map["user"] = user1
        map["orderCancelled"] = "true"
        map["time"] = time
        var reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/messages/"
                    + user1 + "_" + user2
        )

        var reference2 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/messages/"
                    + user2 + "_" + user1
        )
        reference1.push().setValue(map)
        reference2.push().setValue(map)
        var lastMessage = "An Order Cancelled"
        //this way broadcast will update the home screen chat when this user sends a message and
        //realtime db listener will update the home screen chat when the user2 sends a message
        updateLastMessages(user2, time, lastMessage, user1)
        setOrderStatusCancelled(orderID, user1)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    public fun sendOrderConfirmationMessage(orderID:String, user1:String, user2:String) {
        val map: MutableMap<String, Any> = HashMap()
        val time = LocalDateTime.now()
        map["orderID"] = orderID
        map["user"] = user1
        map["orderConfirmed"] = "true"
        map["time"] = time
        var reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/messages/"
                    + user1 + "_" + user2
        )

        var reference2 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/messages/"
                    + user2 + "_" + user1
        )
        reference1.push().setValue(map)
        reference2.push().setValue(map)
//        sendMessageBroadCast("An order Confirmed", time, user2)
        var lastMessage = "An Order Saved"
        //this way broadcast will update the home screen chat when this user sends a message and
        //realtime db listener will update the home screen chat when the user2 sends a message
        updateLastMessages(user2, time, lastMessage, user1)
    }
}

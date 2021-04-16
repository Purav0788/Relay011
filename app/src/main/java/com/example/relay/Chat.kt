package com.example.relay


import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_chat.*
import kotlinx.android.synthetic.main.message_place.*
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap


class Chat : AppCompatActivity() {

    lateinit var reference1: DatabaseReference
    lateinit var reference2: DatabaseReference
    private lateinit var user1:String //primary user, whose phone it is
    private lateinit var user2:String //secondary user,
    private val LAUNCH_ORDER_LIST :Int = 2
    private val LAUNCH_ORDER_SENT:Int = 3
    private val LAUNCH_ORDER_CONFIRMED:Int = 4
    private val LAUNCH_INVOICE:Int = 5
    private val LAUNCH_MYORDERS:Int = 6
    private val LAUNCH_ORDER_CANCEL :Int = 7

    override fun onCreate(savedInstanceState: Bundle?) {
        //expecting intent with the username_usernameofchatter, this string is expected
        //expecting the same intent as UserDetails
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
        user1 = intent.getStringExtra("user1")!!
        user2 = intent.getStringExtra("user2")!!

        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/messages/"
                + user1 + "_" + user2)

        reference2 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/messages/"
                + user2 + "_" + user1)

        //surprisingly this listener, reloads all the messages on create of the activity and effectively duplicates
        //reloadAllPastMessages
        reference1.addChildEventListener(object : ChildEventListener {
            override fun onChildAdded(dataSnapshot: DataSnapshot, s: String?) {
                val map = dataSnapshot.value as Map<String, Any>
                //checking if the message is actually an order:
                if(map["orderID"] != null){
                    val orderID = map["orderID"].toString()
                    val userName = map["user"].toString()
                    if((map["orderConfirmed"] != null)&&(map["orderCanceled"] == null)){
                        if(userName == user1){
                            addOrderConfirmedBox(orderID, 1)
                        }
                        else{
                            addOrderConfirmedBox(orderID, 2)
                        }
                    }
                    if(map["orderCanceled"]!= null){
                        if(userName == user1){
                            addOrderCanceledBox(orderID, 1)
                        }
                        else{
                            addOrderCanceledBox(orderID, 2)
                        }
                    }
                    else{
                        if(userName == user1){
                            addOrderBox(orderID, 1)
                        }
                        else{
                            addOrderBox(orderID, 2)
                        }
                    }
                }
                else{
                    val message = map["message"].toString()
                    val userName = map["user"].toString()
                    if (userName == user1){
                        addMessageBox(message, 1)
                    } else{
                        addMessageBox(message, 2)
                    }
                }
            }

            override fun onChildChanged(
                dataSnapshot: DataSnapshot,
                s: String?
            ) {
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

            override fun onChildMoved(
                dataSnapshot: DataSnapshot,
                s: String?
            ) {
            }
        })
       setActionBar(user2)
    }
    private fun addOrderCanceledBox(orderID:String, type:Int){
        val orderButton = Button(this)
        var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        orderButton.text = "Order Canceled"
        if(type == 1){
            params.gravity = Gravity.RIGHT
        }
        else{
            params.gravity = Gravity.LEFT
        }
        orderButton.layoutParams = params

        orderButton.setTag(R.id.myOrderId, orderID)
//        orderButton.setOnClickListener(View.OnClickListener {
//            val v1 = it
//            if(type == 1 ){
//                //TYPE 1 MEANS the user who sent this message is looking at it
//                //so the order confirmed box is sent by the supplier
//                //so if he clicks at it, he should be taken to invoice
//                Log.d("launching Invoice", "hi")
//                val intent = Intent(this@Chat, invoice::class.java)
//                val orderID:String = v1.getTag(R.id.myOrderId) as String
//                intent.putExtra("orderID",orderID)
//                startActivityForResult(intent, LAUNCH_INVOICE)
//            }else{
//                //take the buyer to go to myOrders, which is not yet implemented
////                val intent = Intent(this@Chat, myOrders::class.java)
////                val orderID:String = v1.getTag(R.id.myOrderId) as String
////                intent.putExtra("orderID",orderID)
////                startActivityForResult(intent,LAUNCH_MYORDERS)
//                Log.d("launching Invoice", "bye")
////                here order cancel will be launched
//                val intent = Intent(this@Chat, orderCancel::class.java)
//                val orderID:String = v1.getTag(R.id.myOrderId) as String
//                intent.putExtra("orderID",orderID)
//                startActivityForResult(intent, LAUNCH_ORDER_CANCEL)
//
//            }
//        })
        layout1.addView(orderButton)
        scrollView.fullScroll(View.FOCUS_DOWN)
    }
    fun addMessageBox(message: String?, type: Int) {
        val textView = TextView(this@Chat)
        textView.text = message
        val lp2 = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp2.weight = 7.0f
        if (type == 1) {
            lp2.gravity = Gravity.RIGHT
//            textView.setBackgroundResource()
        } else {
            lp2.gravity = Gravity.LEFT
//            textView.setBackgroundResource(R.drawable.bubble_out)
        }
        textView.layoutParams = lp2
        //the layout1 and scrollview are ids of the parent elements
        layout1.addView(textView)
        scrollView.fullScroll(View.FOCUS_DOWN)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun sendMessage(v: View?) {
        val messageText = messageArea.getText().toString()
        if (messageText != "") {
            val map: MutableMap<String, Any> = HashMap()
            val time = LocalDateTime.now()
            map["message"] = messageText
            map["user"] = user1
            map["time"] = time
            reference1.push().setValue(map)
            reference2.push().setValue(map)
            messageArea.setText("")
            sendMessageBroadCast(messageText,time, user2)
        }
    }

    public fun openOrder(view:View){
        val intent = Intent(this@Chat, orderList::class.java)
        intent.putExtra("user1", user1)
        intent.putExtra("user2", user2)
        startActivityForResult(intent, LAUNCH_ORDER_LIST);
    }
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LAUNCH_ORDER_LIST) {
            if (resultCode == Activity.RESULT_OK) {
                val result:UUID = data!!.getSerializableExtra("result")as UUID
                saveOrderUUID(result)
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }

        if(requestCode == LAUNCH_ORDER_SENT) {
            //do nothing for now
        }

        if(requestCode == LAUNCH_ORDER_CONFIRMED){
        //this happens when supplier clicks on ordersent box which buyer created
            // here i am supposed to either add to the db the
            if (resultCode == Activity.RESULT_OK) {
                val result:String = data!!.getStringExtra("result")!!
               var uuidResult = UUID.fromString(result)
                saveOrderConfirmation(uuidResult)
                Log.d("I am here","in here in activity for order confirmation")
            }
        }

        if(requestCode == LAUNCH_INVOICE){
        //this happens when supplier clicks on orderConfirmed box which he himself created
        }
        if(requestCode == LAUNCH_MYORDERS){

        }

        if(requestCode == LAUNCH_ORDER_CANCEL){
            //this happens when the user cancels the order
            if (resultCode == Activity.RESULT_OK){

            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveOrderUUID(result:UUID){
        //make it atomic with the orderplacement, its like a transaction,what would happen if this
        //call doesnt happen, but the order gets placed
        val map: MutableMap<String, Any> = HashMap()
        val time = LocalDateTime.now()
        map["orderID"] = result.toString()
        map["user"] = user1
        map["time"] = time
        reference1.push().setValue(map)
        reference2.push().setValue(map)
        sendMessageBroadCast("An Order Saved", time, user2)
    }

    public fun addOrderBox(orderID:String, type:Int){

        //get a custom button + text view
        // as we have id , get the data about that order from the backend, and then
        //need to figure out as quickly as i can about the coroutine,with realtime db firebase, or this stuff
        //is gonna go to hell
        val orderButton = Button(this)
        var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        orderButton.text = "Order Sent"

        if(type == 1){
            params.gravity = Gravity.RIGHT
        }
        else{
            params.gravity = Gravity.LEFT
        }
        orderButton.layoutParams = params

        orderButton.setTag(R.id.myOrderId, orderID)
        orderButton.setOnClickListener(View.OnClickListener {
            val v1 = it
            //check if the user1 is the guy who sent this message,if so send him to Order Sent
            //So if type is 1 then send to orderSent
            //otherwise send him to orderConfirmed
            if(type == 1 ){
                val intent = Intent(this@Chat, orderSent::class.java)
                val orderID:String = v1.getTag(R.id.myOrderId) as String
                intent.putExtra("orderID",orderID)
                startActivityForResult(intent, LAUNCH_ORDER_SENT)
            }else{
                val intent = Intent(this@Chat,orderConfirmed::class.java)
                val orderID:String = v1.getTag(R.id.myOrderId) as String
                intent.putExtra("orderID",orderID)
                startActivityForResult(intent,LAUNCH_ORDER_CONFIRMED)
            }
        })
        layout1.addView(orderButton)
        scrollView.fullScroll(View.FOCUS_DOWN)
    }

    private fun addOrderConfirmedBox(orderID: String, type:Int){
        val orderButton = Button(this)
        var params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        orderButton.text = "Order Confirmed"
        if(type == 1){
            params.gravity = Gravity.RIGHT
        }
        else{
            params.gravity = Gravity.LEFT
        }
        orderButton.layoutParams = params

        orderButton.setTag(R.id.myOrderId, orderID)
        orderButton.setOnClickListener(View.OnClickListener {
            val v1 = it
            if(type == 1 ){
                //TYPE 1 MEANS the user who sent this message is looking at it
                //so the order confirmed box is sent by the supplier
                //so if he clicks at it, he should be taken to invoice
                Log.d("launching Invoice", "hi")
                val intent = Intent(this@Chat, invoice::class.java)
                val orderID:String = v1.getTag(R.id.myOrderId) as String
                intent.putExtra("orderID",orderID)
                startActivityForResult(intent, LAUNCH_INVOICE)
            }else{
                //take the buyer to go to myOrders, which is not yet implemented
//                val intent = Intent(this@Chat, myOrders::class.java)
//                val orderID:String = v1.getTag(R.id.myOrderId) as String
//                intent.putExtra("orderID",orderID)
//                startActivityForResult(intent,LAUNCH_MYORDERS)
                Log.d("launching Invoice", "bye")
//                here order cancel will be launched
                val intent = Intent(this@Chat, orderCancel::class.java)
                val orderID:String = v1.getTag(R.id.myOrderId) as String
                intent.putExtra("orderID",orderID)
                startActivityForResult(intent, LAUNCH_ORDER_CANCEL)

            }
        })
        layout1.addView(orderButton)
        scrollView.fullScroll(View.FOCUS_DOWN)
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveOrderConfirmation(result:UUID){
        val map: MutableMap<String, Any> = HashMap()
        val time = LocalDateTime.now()
        map["orderID"] = result.toString()
        map["user"] = user1
        map["orderConfirmed"] = "true"
        map["time"] = time
        reference1.push().setValue(map)
        reference2.push().setValue(map)
        sendMessageBroadCast("An order Confirmed",time, user2 )
    }

    private fun setActionBar(phoneNumber: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query = reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
                        val actionBar = supportActionBar
                        actionBar!!.title = user.child("name").value as String
                    }
                }
                else{
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun sendMessageBroadCast(lastMessageText:String, lastMessageTime:LocalDateTime, user2:String){
        Log.d("receiverinChat","hello")
        val intent = Intent("lastMessageData")
        intent.putExtra("lastMessageTime", lastMessageTime.toString())
        intent.putExtra("lastMessageText", lastMessageText)
        intent.putExtra("user2", user2)
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent)
    }

}
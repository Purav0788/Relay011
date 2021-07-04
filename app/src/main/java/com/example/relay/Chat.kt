package com.example.relay


import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.viewbinding.ViewBinding
import com.example.relay.databinding.ActivityChatBinding
import com.example.relay.databinding.LayoutMessageItemBinding
import com.example.relay.databinding.LayoutMessageMineItemBinding
import com.example.relay.databinding.LayoutOrderStatusItemBinding
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_order_cancel.*
import kotlinx.android.synthetic.main.my_orders_list_item.*
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.HashMap


class Chat : AppCompatActivity() {

    lateinit var reference1: DatabaseReference
    lateinit var reference2: DatabaseReference
    private lateinit var user1: String //primary user, whose phone it is
    private lateinit var user2: String //secondary user,
    private val LAUNCH_ORDER_LIST: Int = 2
    private val LAUNCH_ORDER_SENT: Int = 3
    private val LAUNCH_ORDER_CONFIRMED: Int = 4
    private val LAUNCH_INVOICE: Int = 5
    private val LAUNCH_MYORDERS: Int = 6
    private val LAUNCH_ORDER_CANCEL: Int = 7
    private lateinit var binding: ActivityChatBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        //expecting intent with the username_usernameofchatter, this string is expected
        //expecting the same intent as UserDetails
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)


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
                Log.d("popo", "onChildAdded: $dataSnapshot")
                val map = dataSnapshot.value as Map<String, Any>
                //checking if the message is actually an order:
                val user = map["user"].toString()
                val time: HashMap<String, Any> = map["time"] as HashMap<String, Any>
                var type = if (user == user1) 1 else 2
                if (map["orderID"] != null) {
                    // TODO: 6/12/2021 Change this to show time dynamically
                    getDeliveryDate(map, type, time)
                } else {
                    addMessageBox(map, type)
                }
            }

            override fun onChildChanged(
                    dataSnapshot: DataSnapshot,
                    s: String?
            ) {
            }

            override fun onChildRemoved(dataSnapshot: DataSnapshot) {}
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onChildMoved(
                    dataSnapshot: DataSnapshot,
                    s: String?
            ) {
            }
        })
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = ""

        setActionBar(user2)

        binding.messageLayout.messageAreaTextInputLayout.setEndIconOnClickListener {
            sendMessage()
        }

        setUser2PhoneNumber(user2)

    }

    private fun getDeliveryDate(map:Map<String,Any>, type:Int, time: HashMap<String, Any>){
        //this increases the rendering time, can have a copy of order stored
        //in the messages itself, rather than having a separate copy of it
        // as this is creating too many calls to db, even when its cached
        //it should have been there with the messages itself, so there was no need of a
        //second call to the db for each of the order box message
        // store delivery date and items in the message , leave it as it is
        val orderID = map["orderID"].toString()
        var orderRef = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/orders/" + orderID)
        orderRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                var deliveryDate = snapshot.child("deliveryDate").value
                var orderList: DataSnapshot = snapshot.child("orderList")
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
               var totalItems = count.toString()
                if (map["orderCancelled"] == "true") {
                    addOrderCancelledBox(orderID, type, deliveryDate.toString(), totalItems)
                } else if (map["orderConfirmed"] == "true") {
                    addOrderConfirmedBox(orderID, type, deliveryDate.toString(), totalItems)
                } else {
                    addOrderBox(orderID, type, deliveryDate.toString(), totalItems)
                }
            }
        })
    }

    private fun setUser2PhoneNumber(user2:String){
        val user2PhoneNumberView:TextView = findViewById(R.id.yourPhoneNumber) as TextView
        user2PhoneNumberView.text = user2
    }
    private fun addDate(time: HashMap<String, Any>) {
        val date = getTime(time)
        val tvDate = TextView(this);
        tvDate.setTextColor(ContextCompat.getColor(this,R.color.white))
        val lp2 = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        lp2.gravity = Gravity.CENTER_HORIZONTAL
        tvDate.setText(date);

        tvDate.setPadding(pixelToDp(6), 0,pixelToDp(6),0);
        tvDate.background = ContextCompat.getDrawable(this,R.drawable.rectangle_64);
        tvDate.layoutParams = lp2
        binding.layout1.addView(tvDate)

    }

    fun pixelToDp(paddingDp: Int): Int{
        val density = resources.displayMetrics.density
        return (paddingDp * density).toInt();
    }


    private fun addOrderCancelledBox(orderID: String, type: Int, deliveryDate:String, totalItems:String) {
        val date = totalItems + "items | Delivery Date:" + deliveryDate
        val orderButton = Button(this)

        val bindingLayoutOrderStatusItemBinding: LayoutOrderStatusItemBinding =
            LayoutOrderStatusItemBinding.inflate(
                LayoutInflater.from(this@Chat),
                null,
                false
            )
        bindingLayoutOrderStatusItemBinding.tvOrderStatus.text =
            "Order Cancelled"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            bindingLayoutOrderStatusItemBinding.tvOrderStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_baseline_clear_24,
                0
            )
        }
        bindingLayoutOrderStatusItemBinding.dateTime.text = date
        if (type == 1) {
            bindingLayoutOrderStatusItemBinding.root.gravity = Gravity.END
//            params.gravity = Gravity.END
        } else {
            bindingLayoutOrderStatusItemBinding.root.gravity = Gravity.START
        }
        bindingLayoutOrderStatusItemBinding.root.setOnClickListener(View.OnClickListener {
            val v1 = it
            //check if the user1 is the guy who sent this message,if so send him to Order Sent
            //So if type is 1 then send to orderSent
            //otherwise send him to orderReceived1
            if (type == 1) {
                val intent = Intent(this@Chat, orderCancel::class.java)
                val orderID: String = v1.getTag(R.id.myOrderId) as String
                intent.putExtra("orderID", orderID)
                intent.putExtra("user1",user1)
                intent.putExtra("user2", user2)
                startActivityForResult(intent, LAUNCH_ORDER_CANCEL)
            } else {
                val intent = Intent(this@Chat, orderCancel::class.java)
                //order confirmed is basically order received
                val orderID: String = v1.getTag(R.id.myOrderId) as String
                intent.putExtra("orderID", orderID)
                intent.putExtra("user1",user1)
                intent.putExtra("user2", user2)
                startActivityForResult(intent, LAUNCH_ORDER_CANCEL)
            }
        })

        bindingLayoutOrderStatusItemBinding.root.setTag(R.id.myOrderId, orderID)
        binding.layout1.addView(bindingLayoutOrderStatusItemBinding.root)
        binding.scrollView.post {
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun addMessageBox(messageMap:Map<String, Any>, type: Int) {


        val message = messageMap["message"].toString()
        if(message == " "){
            return
        }
        val messageTime : Map<String, Any> = messageMap["time"] as Map<String, Any>
        val messageTimeHour = messageTime["hour"].toString()
        val messageTimeMinute = messageTime["minute"].toString()
        lateinit var hourAndMinute:String
        if(messageTimeHour.toInt() > 12) {
        hourAndMinute = (messageTimeHour.toInt()%12).toString() + ":"+messageTimeMinute + " p.m"
        } else if(messageTimeHour.toInt() == 12){
            hourAndMinute = messageTimeHour + ":"+messageTimeMinute + " p.m."
        } else{
            hourAndMinute = messageTimeHour + ":"+messageTimeMinute + " a.m."
        }

        var viewBindingMessage: ViewBinding?
        if (type == 1) {
            viewBindingMessage =
                LayoutMessageMineItemBinding.inflate(LayoutInflater.from(this@Chat))
            viewBindingMessage.tvMessageMine.text = message
            viewBindingMessage.tvMessageTime.text = hourAndMinute
        } else {
            viewBindingMessage = LayoutMessageItemBinding.inflate(LayoutInflater.from(this@Chat))
            viewBindingMessage.tvMessage.text = message
            viewBindingMessage.tvMessageTime.text = hourAndMinute
        }



        val lp2 = LinearLayout.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )

        if (type == 1) {
        lp2.setMargins(pixelToDp(50),0,0,0)
            lp2.gravity = Gravity.END

        } else {
        lp2.setMargins(0,0,pixelToDp(50),0)
            lp2.gravity = Gravity.START
        }
        viewBindingMessage.root.layoutParams = lp2
//        //the layout1 and scrollview are ids of the parent elements
        binding.layout1.addView(viewBindingMessage.root)
        binding.scrollView.post {
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
        }

    }

    //    @RequiresApi(Build.VERSION_CODES.O)
    fun sendMessage() {
        val messageText = binding.messageLayout.messageArea.text.toString()
        if (!messageText.isBlank()) {
            val map: MutableMap<String, Any> = HashMap()
            val time = LocalDateTime.now()
            map["message"] = messageText
            map["user"] = user1
            map["time"] = time
            reference1.push().setValue(map)
            reference2.push().setValue(map)
            binding.messageLayout.messageArea.setText("")
//            sendMessageBroadCast(messageText, time, user2)
            var lastMessage = messageText

            myHelper.updateLastMessages(user2, time, lastMessage, user1)
        }
    }

    fun openOrder(view: View) {
        val intent = Intent(this@Chat, orderList::class.java)
        intent.putExtra("user1", user1)
        intent.putExtra("user2", user2)
        startActivityForResult(intent, LAUNCH_ORDER_LIST)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LAUNCH_ORDER_LIST) {
            if (resultCode == Activity.RESULT_OK) {
               /* val result: UUID = data!!.getSerializableExtra("result") as UUID
                saveOrderUUID(result)*/
            }
            if (resultCode == Activity.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }

        if (requestCode == LAUNCH_ORDER_SENT) {
            //do nothing for now
        }

        if (requestCode == LAUNCH_ORDER_CONFIRMED) {
            //this happens when supplier clicks on ordersent box which buyer created
            // here i am supposed to either add to the db the
            if (resultCode == Activity.RESULT_OK) {
                val result: String = data!!.getStringExtra("result")!!
                var uuidResult = UUID.fromString(result)
//                saveOrderConfirmation(uuidResult)
                myHelper.sendOrderConfirmationMessage(result,user1, user2)
                Log.d("I am here", "in here in activity for order confirmation")
            }
        }

        if (requestCode == LAUNCH_INVOICE) {
            //this happens when supplier clicks on orderReceived1 box which he himself created
        }
        if (requestCode == LAUNCH_MYORDERS) {

        }

        if (requestCode == LAUNCH_ORDER_CANCEL) {
            //this happens when the user cancels the order
            if (resultCode == Activity.RESULT_OK) {
                /*val result: String = data!!.getStringExtra("result")!!
                var uuidResult = UUID.fromString(result)
//                saveOrderCancellation(uuidResult)
                myHelper.sendOrderConfirmationMessage(result,user1, user2)
                Log.d("I am here", "in here in activity for order confirmation")*/
            }
        }
    }

/*    @RequiresApi(Build.VERSION_CODES.O)
    private fun saveOrderUUID(result: UUID) {
        //make it atomic with the orderplacement, its like a transaction,what would happen if this
        //call doesnt happen, but the order gets placed
        val map: MutableMap<String, Any> = HashMap()
        val time = LocalDateTime.now()
        map["orderID"] = result.toString()
        map["user"] = user1
        map["time"] = time
        reference1.push().setValue(map)
        reference2.push().setValue(map)
//        sendMessageBroadCast("An Order Saved", time, user2)
        var lastMessage = "An Order Saved"
        //this way broadcast will update the home screen chat when this user sends a message and
        //realtime db listener will update the home screen chat when the user2 sends a message
        myHelper.updateLastMessages(user2, time, lastMessage, user1)
    }*/

    fun addOrderBox(orderID: String, type: Int, deliveryDate: String, totalItems: String) {

        //get a custom button + text view
        // as we have id , get the data about that order from the backend, and then
        //need to figure out as quickly as i can about the coroutine,with realtime db firebase, or this stuff
        //is gonna go to hell
        var date = totalItems + "items | Delivery Date:" + deliveryDate

        val bindingLayoutOrderStatusItemBinding: LayoutOrderStatusItemBinding =
            LayoutOrderStatusItemBinding.inflate(
                LayoutInflater.from(this@Chat),
                null,
                false
            )

        val orderButton = Button(this)
//        var params = LinearLayout.LayoutParams(
////            500,
//            ViewGroup.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
//        params.topMargin = 10
//        params.marginEnd = 10
//        orderButton.text = "Order Sent\n" + date

        bindingLayoutOrderStatusItemBinding.tvOrderStatus.text = "Order Sent"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            bindingLayoutOrderStatusItemBinding.tvOrderStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                R.drawable.arrow_right,
                0
            )
        }
        bindingLayoutOrderStatusItemBinding.dateTime.text = date


        if (type == 1) {
            bindingLayoutOrderStatusItemBinding.root.gravity = Gravity.END
//            params.gravity = Gravity.END
        } else {
            bindingLayoutOrderStatusItemBinding.root.gravity = Gravity.START
//            params.gravity = Gravity.START
        }
//        bindingLayoutOrderStatusItemBinding.root.layoutParams = params
//        orderButton.layoutParams = params

        bindingLayoutOrderStatusItemBinding.root.setTag(R.id.myOrderId, orderID)
        bindingLayoutOrderStatusItemBinding.root.setOnClickListener(View.OnClickListener {
            val v1 = it
            //check if the user1 is the guy who sent this message,if so send him to Order Sent
            //So if type is 1 then send to orderSent
            //otherwise send him to orderReceived1
            if (type == 1) {
                val intent = Intent(this@Chat, orderSent::class.java)
                val orderID: String = v1.getTag(R.id.myOrderId) as String
                intent.putExtra("orderID", orderID)
                intent.putExtra("user1",user1)
                intent.putExtra("user2", user2)
                startActivityForResult(intent, LAUNCH_ORDER_SENT)
            } else {
                val intent = Intent(this@Chat, orderReceived0::class.java)
                //order confirmed is basically order received
                val orderID: String = v1.getTag(R.id.myOrderId) as String
                intent.putExtra("orderID", orderID)
                intent.putExtra("user1",user1)
                intent.putExtra("user2", user2)
                startActivityForResult(intent, LAUNCH_ORDER_CONFIRMED)
            }
        })
        binding.layout1.addView(bindingLayoutOrderStatusItemBinding.root)
        binding.scrollView.post {
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

    private fun addOrderConfirmedBox(orderID: String, type: Int,deliveryDate: String,totalItems: String) {
//        val orderButton = Button(this)
//        var params = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.WRAP_CONTENT,
//            ViewGroup.LayoutParams.WRAP_CONTENT
//        )
        val bindingLayoutOrderStatusItemBinding: LayoutOrderStatusItemBinding =
            LayoutOrderStatusItemBinding.inflate(
                LayoutInflater.from(this@Chat),
                null,
                false
            )
        var date = totalItems + "items | Delivery Date:" + deliveryDate
        bindingLayoutOrderStatusItemBinding.tvOrderStatus.text = "Order Confirmed"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            bindingLayoutOrderStatusItemBinding.tvOrderStatus.setCompoundDrawablesRelativeWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_baseline_check_24,
                0
            )
        }
        bindingLayoutOrderStatusItemBinding.dateTime.text = date

//        orderButton.text = "Order Confirmed\n" + date
        if (type == 1) {
            bindingLayoutOrderStatusItemBinding.root.gravity = Gravity.END

//            params.gravity = Gravity.RIGHT
        } else {
            bindingLayoutOrderStatusItemBinding.root.gravity = Gravity.START

//            params.gravity = Gravity.LEFT
        }
//        orderButton.layoutParams = params

        bindingLayoutOrderStatusItemBinding.root.setTag(R.id.myOrderId, orderID)
        bindingLayoutOrderStatusItemBinding.root.setOnClickListener(View.OnClickListener {
            val v1 = it
            if (type == 1) {
                //TYPE 1 MEANS the user who sent this message is looking at it
                //so the order confirmed box is sent by the supplier
                //so if he clicks at it, he should be taken to invoiceuser1

                Log.d("launching Invoice", "hi")
                val intent = Intent(this@Chat, invoiceuser1::class.java)
                val orderID: String = v1.getTag(R.id.myOrderId) as String
                intent.putExtra("orderID", orderID)
                intent.putExtra("user1",user1)
                intent.putExtra("user2",user2)
                startActivityForResult(intent, LAUNCH_INVOICE)
            } else {
                //take the buyer to go to myOrders, which is not yet implemented
//                val intent = Intent(this@Chat, myOrders::class.java)
//                val orderID:String = v1.getTag(R.id.myOrderId) as String
//                intent.putExtra("orderID",orderID)
//                startActivityForResult(intent,LAUNCH_MYORDERS)
                Log.d("launching Invoice", "bye")
//                here order cancel will be launched
                val intent = Intent(this@Chat, invoiceUser2::class.java)
                val orderID: String = v1.getTag(R.id.myOrderId) as String
                intent.putExtra("orderID", orderID)
                intent.putExtra("user1",user1)
                intent.putExtra("user2",user2)
                startActivityForResult(intent, LAUNCH_ORDER_CANCEL)
            }
        })
        binding.layout1.addView(bindingLayoutOrderStatusItemBinding.root)
        binding.scrollView.post {
            binding.scrollView.fullScroll(View.FOCUS_DOWN)
        }
    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun saveOrderConfirmation(result: UUID) {
//        val map: MutableMap<String, Any> = HashMap()
//        val time = LocalDateTime.now()
//        map["orderID"] = result.toString()
//        map["user"] = user1
//        map["orderConfirmed"] = "true"
//        map["time"] = time
//        reference1.push().setValue(map)
//        reference2.push().setValue(map)
////        sendMessageBroadCast("An order Confirmed", time, user2)
//        var lastMessage = "An Order Saved"
//        //this way broadcast will update the home screen chat when this user sends a message and
//        //realtime db listener will update the home screen chat when the user2 sends a message
//        updateChats(user2, time, lastMessage, user1)
//    }

//    @RequiresApi(Build.VERSION_CODES.O)
//    private fun saveOrderCancellation(result: UUID) {
//        val map: MutableMap<String, Any> = HashMap()
//        val time = LocalDateTime.now()
//        map["orderID"] = result.toString()
//        map["user"] = user1
//        map["orderCancelled"] = "true"
//        map["time"] = time
//        reference1.push().setValue(map)
//        reference2.push().setValue(map)
//        sendMessageBroadCast("An order Cancelled", time, user2)
//        var lastMessage = "An Order Cancelled"
//        //this way broadcast will update the home screen chat when this user sends a message and
//        //realtime db listener will update the home screen chat when the user2 sends a message
//        updateChats(user2, time, lastMessage, user1)
//    }

    private fun setActionBar(phoneNumber: String) {
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query =
            reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
//                        val actionBar = supportActionBar
//                        actionBar!!.title = user.child("name").value as String
                        binding.yourBusinessName.text = user.child("name").value as String
                    }
                } else {
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }



    private fun getTime(time: HashMap<String, Any>): String {
        var myTime =
            time["dayOfMonth"].toString() + "/" + time["monthValue"].toString() + "/" + time["year"].toString()
        return myTime
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu_chat, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.action_search -> {
            Toast.makeText(this, "Search clicked", Toast.LENGTH_SHORT).show()
            true
        }

        else -> {
            // If we got here, the user's action was not recognized.
            // Invoke the superclass to handle it.
            super.onOptionsItemSelected(item)
        }
    }

    fun backPressed(view: View) {
        onBackPressed()
    }

}
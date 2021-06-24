package com.example.relay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.order_confirm_list_item.view.*
import java.util.ArrayList

class orderCancel : AppCompatActivity() {
    private lateinit var orderID:String
    lateinit var reference1: DatabaseReference;
    private var layoutID = 1
    private var listOfUnitPrices: ArrayList<Long> = ArrayList<Long>()
    private lateinit var user1:String
    private lateinit var user2:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        orderID = intent.getStringExtra("orderID")!!
        user1 = intent.getStringExtra("user1")!!
        user2 = intent.getStringExtra("user2")!!
        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/orders/$orderID"
        )
        reference1.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {

                Log.d("confirm", " I am here in orderReceived2")
                var notes = snapshot.child("notes").value
                var address = snapshot.child("address").value
                var listOfUnitPricesSnapshot = snapshot.child("listOfUnitPrices")
                var orderList: DataSnapshot = snapshot.child("orderList")
                val delimiter = "#"
                var orderName = ""
                var quantity:Int
                var unit:String = ""
                var count = 0;
                var priceNumber:Int = 0
                var totalPrice:Long = 0
                for(i in listOfUnitPricesSnapshot.children){
                    if(i != null){
                        var t = i.value as Long
                        totalPrice += t
                        listOfUnitPrices.add(t)
                        Log.d("t", "${t}")
                    }
                }

                if(listOfUnitPrices.size !=0 ){
                    setContentView(R.layout.activity_order_cancel_and_confirmed)
                    setUpTotalPrice(totalPrice.toString())
                    //if the user cancelled the order before confirming
                    //listofUnitPrices will be null from the database
                    if(user1 == snapshot.child("orderTo").value.toString()){
                        //received from
                        setUpBuyerName(snapshot.child("orderFrom").value.toString())
                    }else{
                        //sold to
                        setUpSellerName(snapshot.child("orderTo").value.toString())
                    }
                    setUpOrderID()
                    setUpCancellerName(snapshot.child("orderCancelledBy").value.toString())

                    var deliveryAddress = findViewById(R.id.deliveryAddress) as TextView
                    deliveryAddress.setText(address.toString())
                    var orderNotes = findViewById(R.id.orderNotes) as TextView
                    orderNotes.setText(notes.toString())

                    for ( i in orderList.children){
                        if ( i!= null) {
                            var t = i.value as String
                            count++;
                            val splitString = t.split(delimiter).toTypedArray();
                            orderName =splitString[0]
                            quantity = splitString[1].toInt()
                            unit = splitString[2]
                            addListItem(orderName, quantity, unit, priceNumber)
                            priceNumber++
                        }
                    }
                    setUpTotalItems(count)
                }else if(listOfUnitPrices.size ==0){
                    setContentView(R.layout.activity_order_cancel)
                    //TODO set up the rest of this stuff like order sent,
                    setUpCommonData(snapshot)

                   // in that case dynamically show received from
                    //orderTo and the user1 match then show the Received from orderFrom
                    //if they dont match show the sent to orderTO
                }

            }
        })
    }
    public fun addListItem(orderName:String, quantity:Int, unit:String, priceNumber:Int){
        Log.d("confirm", " I am here in addlistitem")
        var parent = findViewById<View>(R.id.orders) as RelativeLayout
        val neu: View = layoutInflater.inflate(R.layout.order_confirm_list_item, parent, false)
        neu.id = layoutID
        var params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.BELOW, layoutID-1)
        neu.layoutParams = params
        parent.addView(neu)
        neu.orderName.setText(orderName)
        neu.orderQuantity.setText(quantity.toString())
        neu.unit.setText(unit)
        val unitPrice = neu.unitPrice as EditText
        unitPrice.setText(listOfUnitPrices.elementAt(priceNumber).toString())
        Log.d("price and quantity",listOfUnitPrices.elementAt(priceNumber).toString())
        Log.d("quantity", quantity.toString())
        Log.d("priceNumber", priceNumber.toString())
        neu.itemPrice.setText((listOfUnitPrices.elementAt(priceNumber)*quantity).toString())
        layoutID++
    }

    private fun setUpOrderID() {
        var fakeOrderID = orderID.subSequence(1,6)
        val textView = findViewById(R.id._orderId) as TextView
        textView.text = fakeOrderID.toString()
    }

    private fun setUpCancellerName(cancellerNumber:String){
        Log.d("cancellerNumber", cancellerNumber)
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query =
            reference.child("users").orderByChild("phone_number").equalTo(cancellerNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
                        Log.d("here","hi")
                        val textView = findViewById<TextView>(R.id._cancellerName)
                        val cancellerName = user.child("business_name").value as String
                        Log.d("cancellerName",cancellerName)
                        textView.text =  cancellerName
                    }
                } else {
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }
    private fun setUpSellerName(phoneNumber:String) {
        val reference = FirebaseDatabase.getInstance().reference
        val query: Query =
            reference.child("users").orderByChild("phone_number").equalTo(phoneNumber)
        query.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    // dataSnapshot is the "users" node with all children with phone_number = phoneNumber
                    for (user in dataSnapshot.children) {
                        // do something with the individual "user"
                        val textView = findViewById(R.id._buyerName) as TextView
                        textView.text = user.child("business_name").value as String
                    }
                } else {
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
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

        if(user1 == snapshot.child("orderTo").value.toString()){
            //received from
            setUpBuyerName(snapshot.child("orderFrom").value.toString())
        }else{
            //sold to
            setUpSellerName(snapshot.child("orderTo").value.toString())
        }

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
                        val textView = findViewById(R.id._buyerName) as TextView
                        textView.text = user.child("business_name").value as String
                    }
                } else {
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    private fun setUpTotalItems(count:Int){
        val textView4 = findViewById(R.id.items) as TextView
        textView4.text = count.toString()
    }

    private fun setUpTotalPrice(totalPrice:String){
        val textView4 = findViewById(R.id.totalPrice) as TextView
        textView4.text = totalPrice.toString()
    }
}
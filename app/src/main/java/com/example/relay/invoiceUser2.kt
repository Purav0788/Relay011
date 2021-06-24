package com.example.relay

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.annotation.RequiresApi
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.order_confirm_list_item.view.*
import java.util.ArrayList
//user2 means the user who received invoice is looking at it
class invoiceUser2 : AppCompatActivity() {
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
                var isOrderCancelled = snapshot.child("orderCancelled").value.toString()
                var isOrderConfirmed = snapshot.child("orderConfirmed").value.toString()
                Log.d("isOrderCancelled", isOrderCancelled)
                Log.d("isOrderConfirmed", isOrderConfirmed)
                lateinit var orderStatus:String
                if(isOrderCancelled == "true"){
                    orderStatus = "orderCancelled"
                }else if(isOrderConfirmed == "true"){
                    orderStatus = "orderConfirmed"
                }else{
                    orderStatus = "orderSent"
                }
                Log.d("orderStatus", orderStatus)
                if(orderStatus == "orderCancelled"){
                    setContentView(R.layout.activity_invoice_user1_and_cancelled)
                }else{
                    setContentView(R.layout.activity_invoice_user1)
                }
                Log.d("confirm", " I am here in orderReceived2")
                var notes = snapshot.child("notes").value
                var address = snapshot.child("address").value
                var totalPrice = snapshot.child("totalPrice").value
                var totalPriceView = findViewById(R.id.totalPrice) as TextView
                totalPriceView.setText(totalPrice.toString())
                var deliveryDate = snapshot.child("deliveryDate").value
                val textView = findViewById(R.id.deliveryDate) as TextView
                textView.text= deliveryDate.toString()
                setUpSellerName(snapshot.child("orderTo").value.toString())
                var listOfUnitPricesSnapshot = snapshot.child("listOfUnitPrices")
                var deliveryAddress = findViewById(R.id.deliveryAddress) as TextView
                deliveryAddress.setText(address.toString())
                var orderNotes = findViewById(R.id.orderNotes) as TextView
                orderNotes.setText(notes.toString())
                setUpOrderID()
                var orderList: DataSnapshot = snapshot.child("orderList")
                val delimiter = "#"
                var orderName = ""
                var quantity:Int
                var unit:String = ""
                var count = 0;
                var priceNumber:Int = 0
                for(i in listOfUnitPricesSnapshot.children){
                    if(i != null){
                        var t = i.value as Long
                        listOfUnitPrices.add(t)
                        Log.d("t", "${t}")
                    }
                }
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
                        val textView = findViewById(R.id._sellerName) as TextView
                        textView.text = user.child("business_name").value as String
                    }
                } else {
                }
            }
            override fun onCancelled(databaseError: DatabaseError) {}
        })
    }

    @RequiresApi(Build.VERSION_CODES.O)
    public fun _cancelOrder(){

        myHelper.sendOrderCancellationMessage(orderID,user1, user2)
        finish()
    }
    @RequiresApi(Build.VERSION_CODES.O)
    public fun cancelOrder(view:View){
        val dialogClickListener = DialogInterface.OnClickListener { dialog, which ->
            when (which) {
                DialogInterface.BUTTON_POSITIVE -> {
                    _cancelOrder()
                }
                DialogInterface.BUTTON_NEGATIVE -> {
                }
            }
        }

        val builder: AlertDialog.Builder = AlertDialog.Builder(this@invoiceUser2)
        builder.setMessage("Do you want to cancel the order?")
            .setPositiveButton("Yes", dialogClickListener)
            .setNegativeButton("No", dialogClickListener).show()
    }

    private fun setUpTotalItems(count:Int){
        val textView4 = findViewById(R.id.items) as TextView
        textView4.text = count.toString()
    }
}
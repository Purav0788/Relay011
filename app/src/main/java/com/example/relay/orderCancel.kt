package com.example.relay

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.order_confirm2_list_item.view.*
import kotlinx.android.synthetic.main.order_confirm_list_item.view.*
import kotlinx.android.synthetic.main.order_confirm_list_item.view.itemPrice
import kotlinx.android.synthetic.main.order_confirm_list_item.view.orderName
import kotlinx.android.synthetic.main.order_confirm_list_item.view.orderQuantity
import kotlinx.android.synthetic.main.order_confirm_list_item.view.unit
import kotlinx.android.synthetic.main.order_confirm_list_item.view.unitPrice
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap
import kotlin.properties.Delegates

class orderCancel : AppCompatActivity() {
    private lateinit var orderID: String
    lateinit var reference1: DatabaseReference;
    private var layoutID = 1

    //remember we dont get string but rather long from teh database, and thus if we declare it
    //as string over here, it always crashes saying cant cast long as string
    //just change the declaration here to Long and then use .toString to conver that
    private var listOfUnitPrices: ArrayList<Long> = ArrayList<Long>()
    private lateinit var totalPrice: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_cancel)

        orderID = intent.getStringExtra("orderID")!!


        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/orders/$orderID"
        )


        //as this is a listener realtime , so when we press on confirm order, it takes that entry as soon as
        //as that button is pressed and showing bad behaviour
        //so chnaged it to add listener for single value event
        reference1.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("confirm", " I am here in orderConfirmed2")
                var notes = snapshot.child("notes").value
                var address = snapshot.child("address").value
                var deliveryAddress = findViewById(R.id.deliveryAddress) as TextView
                deliveryAddress.setText(address.toString())
                var orderNotes = findViewById(R.id.orderNotes) as TextView
                orderNotes.setText(notes.toString())
                var orderList: DataSnapshot = snapshot.child("orderList")
                listOfUnitPrices =
                    snapshot.child("listOfUnitPrices").value as ArrayList<Long>
                totalPrice = snapshot.child("totalPrice").value.toString()
                var totalPriceView = findViewById(R.id.totalPrice) as TextView
                totalPriceView.setText(totalPrice)

                val delimiter = "#"
                var orderName = ""
                var quantity: Int
                var unit: String = ""
                var count = 0;
                var priceNumber: Int = 0
                for (i in orderList.children) {
                    if (i != null) {
                        var t = i.value as String
                        count++;
                        val splitString = t.split(delimiter).toTypedArray();
                        orderName = splitString[0]
                        quantity = splitString[1].toInt()
                        unit = splitString[2]
                        addListItem(
                            orderName,
                            quantity,
                            unit,
                            priceNumber
                        )
                        priceNumber++
                    }
                }

//                for (i in listOfUnitPricesSnapshot.children) {
//                    if (i != null) {
//                        var t = i.value as String
//
//                    }
//                }
            }
        })
    }

    public fun addListItem(
        orderName: String,
        quantity: Int,
        unit: String,
        priceNumber: Int
    ) {
        Log.d("confirm", " I am here in addlistitem")
        var parent = findViewById<View>(R.id.orders) as RelativeLayout
        val neu: View = layoutInflater.inflate(R.layout.order_confirm_list_item, parent, false)
        neu.id = layoutID
        var params = RelativeLayout.LayoutParams(
            RelativeLayout.LayoutParams.WRAP_CONTENT,
            RelativeLayout.LayoutParams.WRAP_CONTENT
        )
        params.addRule(RelativeLayout.BELOW, layoutID - 1)
        neu.layoutParams = params
        parent.addView(neu)
        neu.orderName.setText(orderName)
        neu.orderQuantity.setText(quantity.toString())
        neu.unit.setText(unit)
        val unitPrice = neu.unitPrice as EditText
        var unitPriceValue = listOfUnitPrices.elementAt(priceNumber).toString()
        unitPrice.setText(unitPriceValue)
        Log.d("price and quantity", listOfUnitPrices.elementAt(priceNumber).toString())
        Log.d("quantity", quantity.toString())
        Log.d("priceNumber", priceNumber.toString())
        neu.itemPrice.setText((listOfUnitPrices.elementAt(priceNumber).toInt() * quantity).toString())
        layoutID++
    }

    public fun cancelOrder(view: View) {
        var orderCancelled: String = "true"
        val myMap: MutableMap<String, Any> = HashMap()
        myMap["orderCancelled"] = orderCancelled
        reference1.updateChildren(myMap)
        val intent = Intent()
        intent.putExtra("result", orderID)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }
}


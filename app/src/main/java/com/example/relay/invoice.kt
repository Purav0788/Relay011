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

class invoice : AppCompatActivity() {
    private lateinit var orderID:String
    lateinit var reference1: DatabaseReference;
    private var layoutID = 1
    private var listOfUnitPrices: ArrayList<Long> = ArrayList<Long>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_invoice)
        orderID = intent.getStringExtra("orderID")!!
        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
                "https://relay-28f2e-default-rtdb.firebaseio.com/orders/$orderID"
        )
        reference1.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("confirm", " I am here in orderConfirmed2")
                var notes = snapshot.child("notes").value
                var address = snapshot.child("address").value
                var listOfUnitPricesSnapshot = snapshot.child("listOfUnitPrices")
                var deliveryAddress = findViewById(R.id.deliveryAddress) as TextView
                deliveryAddress.setText(address.toString())
                var orderNotes = findViewById(R.id.orderNotes) as TextView
                orderNotes.setText(notes.toString())
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
}
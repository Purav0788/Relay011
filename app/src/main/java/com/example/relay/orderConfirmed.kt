package com.example.relay

//import android.R
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.order_confirm_list_item.view.*
import kotlinx.android.synthetic.main.order_confirm_list_item.view.orderName
import kotlinx.android.synthetic.main.order_confirm_list_item.view.orderQuantity

//import kotlinx.android.synthetic.main.order_list_item.*


class orderConfirmed : AppCompatActivity() {
    private var layoutID = 1
    private lateinit var orderID:String
    lateinit var reference1: DatabaseReference
    private var listOfUnitPrices:ArrayList<kotlin.Int> = ArrayList<kotlin.Int>()
    private var LAUNCH_ORDER_CONFIRMED2:Int = 2
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_confirmed)
        orderID = intent.getStringExtra("orderID")!!
        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
                "https://relay-28f2e-default-rtdb.firebaseio.com/orders/$orderID"
        )
        reference1.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {
            }
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("confirm", " I am here")
                var orderList: DataSnapshot = snapshot.child("orderList")
                val delimiter = "#"
                var orderName = ""
                var quantity:Int
                var unit:String = ""
                var count = 0;
                for ( i in orderList.children){
                    if ( i!= null) {
                        var t = i.value as String
                        count++;
                        val splitString = t.split(delimiter).toTypedArray();
                        orderName =splitString[0]
                        quantity = splitString[1].toInt()
                        unit = splitString[2]
                        addListItem(orderName, quantity, unit)
                    }
                }
            }
        })
    }
    //take item name and quantity and add that many items accordingly for the for loop
    //and take the price from supplier
    public fun addListItem(orderName:String, quantity:Int, unit:String){
        var parent = findViewById<View>(R.id.orderList) as RelativeLayout
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
        unitPrice.addTextChangedListener(object : TextWatcher {

            override fun afterTextChanged(s: Editable) {}

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence, start: Int,
                                       before: Int, count: Int) {
                Log.d("mystring","${s}")
                if(s.isNotEmpty()){
                    Log.d("mystring","${s}")
                    neu.itemPrice.setText((s.toString().toInt()*neu.orderQuantity.orderQuantity.text.toString().toInt()).toString())
                }

            }
        })
        layoutID++
    }

    public fun quotePrice(view: View){
            Log.d("orderlist", "I am here")
            for(i:Int in 1..layoutID-1){
                Log.d("orderlist", "I am in for loop")
                var orderUnitPrice:String
                var newOrderItem:View = findViewById<View>(i)
                orderUnitPrice = newOrderItem.unitPrice.text.toString()
                Log.d("orderlist", orderUnitPrice)

                listOfUnitPrices.add(orderUnitPrice.toInt())
                Log.d("orderlistindex", listOfUnitPrices.elementAt(0).toString())
            }
            val intent = Intent(this@orderConfirmed, orderConfirmed2::class.java)
            intent.putExtra("listOfUnitPrices", listOfUnitPrices)
            intent.putExtra("orderID", orderID)
            startActivityForResult(intent, LAUNCH_ORDER_CONFIRMED2)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == LAUNCH_ORDER_CONFIRMED2) {
            if (resultCode == Activity.RESULT_OK) {
                Log.d("confirmed2","here")
                setResult(Activity.RESULT_OK,data);
                finish()
            }
            if (resultCode == Activity.RESULT_CANCELED) {

            }
        }
    }
}
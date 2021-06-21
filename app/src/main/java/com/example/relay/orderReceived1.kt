package com.example.relay

//import android.R
import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.order_confirm_list_item.view.*
import kotlinx.android.synthetic.main.order_confirm_list_item.view.orderName
import kotlinx.android.synthetic.main.order_confirm_list_item.view.orderQuantity

//import kotlinx.android.synthetic.main.order_list_item.*

//Order Received Activity

class orderReceived1 : AppCompatActivity() {
    private var layoutID = 1
    private lateinit var orderID:String
    lateinit var reference1: DatabaseReference
    private var listOfUnitPrices:ArrayList<kotlin.Int> = ArrayList<kotlin.Int>()
    private var LAUNCH_ORDER_CONFIRMED2:Int = 2
    private var totalPrice:Int = 0
    private lateinit var user1:String
    private lateinit var user2:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_received1)
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
                Log.d("confirm", " I am here")
                var notes = snapshot.child("notes").value
                var address = snapshot.child("address").value
                var deliveryAddress = findViewById(R.id.deliveryAddress) as TextView
                deliveryAddress.setText(address.toString())
                var orderNotes = findViewById(R.id.orderNotes) as TextView
                orderNotes.setText(notes.toString())
                var orderList: DataSnapshot = snapshot.child("orderList")
                var deliveryDate = snapshot.child("deliveryDate").value
                val textView = findViewById(R.id.deliveryDate) as TextView
                textView.text= deliveryDate.toString()

                setUpOrderID()
                setUpBuyerName(snapshot.child("orderFrom").value.toString())
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

            override fun afterTextChanged(s: Editable) {
                totalPrice = totalPrice + s.toString().toInt()*neu.orderQuantity.orderQuantity.text.toString().toInt()
                var totalPriceView = findViewById(R.id.totalPrice) as EditText
                totalPriceView.setText(totalPrice.toString())
            }

            override fun beforeTextChanged(s: CharSequence, start: Int,
                                           count: Int, after: Int) {
                totalPrice = totalPrice - s.toString().toInt()*neu.orderQuantity.orderQuantity.text.toString().toInt()
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

    public fun confirmOrder(view: View){
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
        var totalPriceSetByUser = findViewById<EditText>(R.id.totalPrice).text.toString()

        var orderConfirmed:String = "true"
        val myMap: MutableMap<String, Any> = HashMap()
        myMap["listOfUnitPrices"] = listOfUnitPrices
        myMap["totalPrice"] = totalPrice
        myMap["orderConfirmed"] = orderConfirmed
        val query:Query = reference1
        query.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            @RequiresApi(Build.VERSION_CODES.O)
            override fun onDataChange(snapshot: DataSnapshot) {
                if(snapshot.exists()){
                    var myOrderMap = snapshot.value as HashMap<String, Any>
                    Log.d("myOrderMap", myOrderMap.toString())
                    if(myOrderMap["orderConfirmed"] == "false"){
                        reference1.updateChildren(myMap)
                        val intent = Intent()
                        intent.putExtra("result", orderID)
                        setResult(Activity.RESULT_OK,intent)
                        myHelper.sendOrderConfirmationMessage(orderID,user1,user2)
                        finish()
                    }else{
                        Toast.makeText(this@orderReceived1, "order is already confirmed once!",
                            Toast.LENGTH_LONG).show();
                    }

                }
            }

        })

    }

    private fun setUpOrderID() {
        var fakeOrderID = orderID.subSequence(1,6)
        val textView = findViewById(R.id._orderId) as TextView
        textView.text = fakeOrderID.toString()
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
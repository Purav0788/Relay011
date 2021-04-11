package com.example.relay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import com.google.firebase.database.*

class orderSent : AppCompatActivity() {
    private lateinit var orderID:String
    lateinit var reference1: DatabaseReference;
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_sent)
        orderID = intent.getStringExtra("orderID")!!
        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/orders/$orderID"
        )
        reference1.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError) {
            }

            override fun onDataChange(snapshot: DataSnapshot) {
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
                textView4.text = count.toString();
            }

        })

    }
}
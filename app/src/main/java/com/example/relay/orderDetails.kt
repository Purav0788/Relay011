package com.example.relay

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import java.util.*


class orderDetails : AppCompatActivity() {
    private var deliveryDate ="";
    lateinit var reference1: DatabaseReference;
    private var orderList:ArrayList<kotlin.String?>? = ArrayList<kotlin.String?>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Change needed here where copied into original source code
        setContentView(R.layout.activity_main)
        orderList = (intent.getSerializableExtra("listOfOrders") as ArrayList<kotlin.String?>?)
        val textview = findViewById(R.id.order) as TextView
        val delimiter = '#'

        var orders = "";
//        Appending all the orders into a string to be shown in the text view
        for (i in this!!.orderList!!)
        {
            if ( i!= null) {
                val splitString = i.split(delimiter).toTypedArray();
                orders+=splitString[0]+" "+splitString[1]+" "+splitString[2]
                orders += '\n';
            }
        }
        textview.text = orders;
        val calendarView = findViewById<CalendarView>(R.id.calendarView)
        calendarView?.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Note that months are indexed from 0. So, 0 means January, 1 means february, 2 means march etc.
            val msg = "" + dayOfMonth + "/" + (month + 1) + "/" + year
            deliveryDate = msg;
//            Change needed here ( context needs to be changed )
//            Toast.makeText(this@orderDetails, msg, Toast.LENGTH_SHORT).show()
        }
        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/")

    }

    public fun placeOrder(myview: View){
        val textView = findViewById(R.id.address) as TextView
        var addressEntered :String = textView.text.toString()
        val textView1 = findViewById(R.id.notes) as TextView
        var notesEntered : String = textView1.text.toString()
        var key = UUID.randomUUID()
        reference1.child("orders").child(key.toString()).setValue(Record(deliveryDate,addressEntered,notesEntered, orderList ))

        val intent = Intent()
        val result = key
        intent.putExtra("result", result)
        setResult(Activity.RESULT_OK,intent)
        finish()
    }
}
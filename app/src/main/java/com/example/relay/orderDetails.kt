package com.example.relay

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_order_details.*
import kotlinx.android.synthetic.main.activity_order_sent.*
import java.util.*


class orderDetails : AppCompatActivity() {
    val MONTHS = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul","Aug", "Sep", "Oct", "Nov", "Dec");

    private var deliveryDate = ""
    lateinit var reference1: DatabaseReference;
    private var orderList: ArrayList<kotlin.String> = ArrayList<kotlin.String>()
    private lateinit var user1: String
    private lateinit var user2: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Change needed here where copied into original source code
        setContentView(R.layout.activity_order_details)
        // TODO: 6/8/2021 Uncomment this
        orderList = (intent.getSerializableExtra("listOfOrders") as ArrayList<kotlin.String>)
        user1 = intent.getStringExtra("user1")!!
        user2 = intent.getStringExtra("user2")!!
        val textview = findViewById<TextView>(R.id.order)
        val delimiter = '#'

        var orders = "";

        for (i in this.orderList) {
            val splitString = i.split(delimiter).toTypedArray();
            orders += splitString[0] + " " + splitString[1] + " " + splitString[2]
            orders += '\n';
        }
        textview.text = orders;

        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/"
        )
        ibEditDeliveryDate.setOnClickListener{
            showDatePickerDialog();
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDatePickerDialog() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dpd = DatePickerDialog(this@orderDetails, DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->


            // Display Selected date in textbox
            deliveryDate = "$dayOfMonth ${MONTHS[monthOfYear]}, $year";
            dateTime.setText(deliveryDate)

        }, year, month, day)

        dpd.show()
    }

    public fun placeOrder(myview: View) {
        val textView = findViewById<TextView>(R.id.address)
        val addressEntered: String = textView.text.toString()
        val textView1 = findViewById<TextView>(R.id.notes)
        val notesEntered: String = textView1.text.toString()
        val key = UUID.randomUUID()
        val orderConfirmed = "false"
        val orderSent = "true"
        val orderCancelled = "false"
        reference1.child("orders").child(key.toString()).setValue(
            Record(
                deliveryDate,
                addressEntered,
                notesEntered,
                orderList,
                user1,
                user2,
                orderConfirmed,
                orderSent,
                orderCancelled
            )
        )

        val intent = Intent()
        val result = key
        intent.putExtra("result", result)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun backPressed(view: View) {
        onBackPressed()
    }
}
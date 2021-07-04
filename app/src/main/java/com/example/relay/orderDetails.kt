package com.example.relay

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.CalendarView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.example.relay.databinding.OrderDetailsListItemBinding
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_order_details.*
import kotlinx.android.synthetic.main.activity_order_sent.*
import java.util.*


class orderDetails : AppCompatActivity() {
    val MONTHS =
        arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")


    private var deliveryDate = ""
    lateinit var reference1: DatabaseReference
    private var orderList: ArrayList<kotlin.String> = ArrayList<kotlin.String>()
    private lateinit var user1: String
    private lateinit var user2: String
    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        Change needed here where copied into original source code
        setContentView(R.layout.activity_order_details)
        orderList = (intent.getSerializableExtra("listOfOrders") as ArrayList<kotlin.String>)
        user1 = intent.getStringExtra("user1")!!
        user2 = intent.getStringExtra("user2")!!
        orderItemsQty.text = "${orderList.size} items"
        val delimiter = '#'
        var orders = ""

        for (i in this.orderList) {
            Log.d("popo", "onCreate: $i")
            val splitString = i.split(delimiter).toTypedArray()
            val orderDetailsListItemBinding: OrderDetailsListItemBinding = OrderDetailsListItemBinding.inflate(layoutInflater);
            orderDetailsListItemBinding.tvItemName.text = splitString[0]
            orderDetailsListItemBinding.tvItemQty.text = splitString[1] + " " + splitString[2]
            expandedLayoutContainer.addView(orderDetailsListItemBinding.root)
        }
        reference1 = FirebaseDatabase.getInstance().getReferenceFromUrl(
            "https://relay-28f2e-default-rtdb.firebaseio.com/"
        )
        ibEditDeliveryDate.setOnClickListener {
            showDatePickerDialog()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showDatePickerDialog() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)


        val dpd = DatePickerDialog(
            this@orderDetails,
            DatePickerDialog.OnDateSetListener { view, year, monthOfYear, dayOfMonth ->


                // Display Selected date in textbox
                deliveryDate = "$dayOfMonth ${MONTHS[monthOfYear]}, $year"
                dateTime.setText(deliveryDate)

            },
            year,
            month,
            day
        )

        dpd.show()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun placeOrder(myview: View) {
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
        myHelper.sendOrderSentMessage(result, user1, user2)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    fun backPressed(view: View) {
        onBackPressed()
    }

    fun expandCollapseItems(view: View) {
        if (expandableLayout.isExpanded) {
            expandableLayout.collapse(true)
            orderItemsQty.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_baseline_arrow_drop_up_24,
                0
            )
        } else
            expandableLayout.expand(true)
            orderItemsQty.setCompoundDrawablesWithIntrinsicBounds(
                0,
                0,
                R.drawable.ic_baseline_arrow_drop_down_24,
                0
            )
    }
}
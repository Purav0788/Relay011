package com.example.relay

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle

class orderSent : AppCompatActivity() {
    private lateinit var orderID:String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_order_sent)
        orderID = intent.getStringExtra("orderID")!!

    }
}
package com.example.relay

class Record {

    var orderList: ArrayList<kotlin.String> = ArrayList<kotlin.String>()
    lateinit var deliveryDate: String
    lateinit var address: String
    lateinit var notes: String
    lateinit var orderCancelled: String
    lateinit var orderConfirmed: String
    lateinit var orderFrom: String
    lateinit var orderSent: String
    lateinit var orderTo: String

    constructor(
        deliveryDate: String,
        address: String,
        notes: String,
        orderList: java.util.ArrayList<String>,
        orderFrom: String,
        orderTo:String,
        orderConfirmed:String,
        orderSent:String,
        orderCancelled:String) {
        this.address = address;
        this.deliveryDate = deliveryDate;
        this.notes = notes
        this.orderList = orderList
        this.orderCancelled = orderCancelled
        this.orderConfirmed = orderConfirmed
        this.orderFrom = orderFrom
        this.orderTo = orderTo
        this.orderSent = orderSent
    }
}
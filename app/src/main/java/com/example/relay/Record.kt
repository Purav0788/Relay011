package com.example.relay

class Record {

    var orderList:ArrayList<kotlin.String> = ArrayList<kotlin.String>()
    lateinit var deliveryDate: String
    lateinit var address : String
    lateinit var notes : String
    constructor(deliveryDate: String, address: String, notes: String, orderList: java.util.ArrayList<String>)
    {
        this.address = address;
        this.deliveryDate = deliveryDate;
        this.notes = notes
        this.orderList = orderList
    }
}
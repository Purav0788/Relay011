package com.example.relay

class orderPricesRecordClass {
        var listOfUnitPrices:ArrayList<kotlin.Int> = ArrayList<kotlin.Int>()
        private lateinit var totalPrice:String
        private lateinit var orderConfirmed:String
        constructor(listOfUnitPrices: java.util.ArrayList<Int>, totalPrice:String, orderConfirmed:String)
        {
            this.listOfUnitPrices = listOfUnitPrices
            this.totalPrice = totalPrice
            this.orderConfirmed = orderConfirmed
        }
}
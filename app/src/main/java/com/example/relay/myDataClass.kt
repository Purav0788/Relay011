package com.example.relay

import java.time.LocalTime

public class myDataClass(
    private  var time: LocalTime,
    private  var lastMessage: String,
    private  var name: String,
    private  var phoneNumber: String
) {
    public fun getName():String{
        return name
    }
    public fun getTime():LocalTime{
        return time
    }
    public fun getphoneNumber():String{
        return phoneNumber
    }
    public fun getLastMessage():String{
        return lastMessage
    }
    public fun setName(name:String){
        this.name = name
    }
    public fun setTime(time:LocalTime){
        this.time = time
    }
    public fun setLastMessage(lastMessage:String){
        this.lastMessage = lastMessage
    }
    public fun setPhoneNumber(phoneNumber: String){
        this.phoneNumber = phoneNumber
    }
}
package com.example.ultimatepearl

data class Master(var id:String, val name:String, val email:String, val contact:String, val area: Area, val addressLine:String, val city:String, val GST_Type:String, val GST_Number:String, val drugLicNo:String, val fssaiNo:Long, val cashDiscount:Float, val orderCount:Int, val unpaidBill:Float, val creditDays:Int, val returnCount:Int)

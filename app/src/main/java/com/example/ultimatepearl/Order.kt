package com.example.ultimatepearl

data class Order(val id:String,val items:MutableList<OrderItem>,val bill:Float,val timeStamp:String,val paymentStatus:Boolean,val salesman:Salesman)

package com.example.ultimatepearl

data class Product(val id:String,val name:String,val productGroup: ProductGroup,val ptrRate:Float,val mrp:Float,val taxRate:Float,val hsnCode:String,val inStock:Int,val scheme:Scheme,val discount:Float,val soldQuantity:Long)

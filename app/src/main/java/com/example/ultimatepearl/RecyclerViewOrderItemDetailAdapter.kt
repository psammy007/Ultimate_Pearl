package com.example.ultimatepearl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ultimatepearl.databinding.OrderItemDetailBinding


class RecyclerViewOrderItemDetailAdapter(private val list: MutableList<OrderItem>,private val flag:Boolean)
    :RecyclerView.Adapter<RecyclerViewOrderItemDetailAdapter.ViewHolder>() {

    private lateinit var binding:OrderItemDetailBinding

    inner class ViewHolder(val binding: OrderItemDetailBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater,R.layout.order_item_detail,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.itemCount.text = list[position].count.toString()
        holder.binding.name.text = list[position].product.name
        holder.binding.freeItemCount.text = list[position].freeCount.toString()
        holder.binding.itemTotal.text =  "\u20B9 " + (list[position].count * list[position].product.mrp).toString()
        if(flag){
            holder.binding.productCode.visibility = View.GONE
        }else {
            holder.binding.code.text = list[position].product.id
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
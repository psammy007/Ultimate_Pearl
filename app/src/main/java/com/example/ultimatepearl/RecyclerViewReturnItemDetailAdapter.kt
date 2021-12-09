package com.example.ultimatepearl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ultimatepearl.databinding.OrderItemDetailBinding

class RecyclerViewReturnItemDetailAdapter (private val list: MutableList<OrderItem>,private val flag:Boolean)
    : RecyclerView.Adapter<RecyclerViewReturnItemDetailAdapter.ViewHolder>(){
    private lateinit var binding: OrderItemDetailBinding

    inner class ViewHolder(val binding: OrderItemDetailBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater,R.layout.order_item_detail,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.run {
            name.text = list[position].product.name
            itemCount.text = list[position].count.toString()

            freeTitle.text = "MRP"
            freeItemCount.text = list[position].product.mrp.toString()

            productCode.visibility = View.GONE
            itemTotal.visibility = View.GONE
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }


}
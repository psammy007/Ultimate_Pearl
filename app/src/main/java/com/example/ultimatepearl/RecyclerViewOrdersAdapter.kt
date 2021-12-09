package com.example.ultimatepearl

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ultimatepearl.databinding.OrderItemBinding

class RecyclerViewOrdersAdapter(private val list: MutableList<OrderTemp>,private val clickEventHandler: ClickEventHandler):
    RecyclerView.Adapter<RecyclerViewOrdersAdapter.ViewHolder>(){

    lateinit var binding: OrderItemBinding

    inner class ViewHolder(val binding: OrderItemBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerViewOrdersAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater,R.layout.order_item,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = list[position].timeStamp
        holder.binding.sunInfo.text = "Total :"
        holder.binding.ordersCount.text = list[position].bill.toString()
        if(list[position].paymentStatus.toString().toBoolean()){
            holder.binding.paymentStatus.setBackgroundResource(R.drawable.ic_success)
        }else{
            holder.binding.paymentStatus.setBackgroundResource(R.drawable.ic_unpaid)
        }

        holder.binding.name.setOnClickListener {
            clickEventHandler.openInDetail(list[position].id)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}
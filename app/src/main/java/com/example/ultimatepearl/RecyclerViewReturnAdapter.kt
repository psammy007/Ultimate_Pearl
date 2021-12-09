package com.example.ultimatepearl

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ultimatepearl.databinding.OrderItemBinding

class RecyclerViewReturnAdapter(private val list: MutableList<Sample>,private val clickEventHandler: ClickEventHandler):
    RecyclerView.Adapter<RecyclerViewReturnAdapter.ViewHolder>(){

    lateinit var binding: OrderItemBinding
    inner class ViewHolder(val binding: OrderItemBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater,R.layout.order_item,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = list[position].name
        holder.binding.sunInfo.text = "Salesman"
        holder.binding.ordersCount.text = list[position].info
        holder.binding.name.setOnClickListener {
            clickEventHandler.openInDetail(list[position].id)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}
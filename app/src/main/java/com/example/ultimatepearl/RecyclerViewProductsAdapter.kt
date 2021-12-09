package com.example.ultimatepearl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ultimatepearl.databinding.RecyclerItemBinding

class RecyclerViewProductsAdapter(private val list: MutableList<Product>, private val eventHandler: ClickEventHandler):
    RecyclerView.Adapter<RecyclerViewProductsAdapter.ViewHolder>() {

    private lateinit var binding: RecyclerItemBinding

    inner class ViewHolder(val binding:RecyclerItemBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater,R.layout.recycler_item,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = list[position].name
        holder.binding.sunInfo.text = "Price :"
        holder.binding.ordersCount.text = "\u20B9" + " " + list[position].mrp.toString()
        holder.binding.increment.visibility = View.GONE
        holder.binding.decrement.visibility = View.GONE
        holder.binding.count.visibility = View.GONE
        holder.binding.freeTitle.visibility = View.GONE
        holder.binding.freeCountHandler.visibility = View.GONE
        holder.binding.stock.text = list[position].inStock.toString()
        holder.binding.name.setOnClickListener {
            eventHandler.openInDetail(list[position].id)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }
}
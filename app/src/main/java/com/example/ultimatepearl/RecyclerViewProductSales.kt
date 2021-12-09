package com.example.ultimatepearl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ultimatepearl.databinding.RecyclerItemBinding

class RecyclerViewProductSales (private val list:MutableList<Sample>):
    RecyclerView.Adapter<RecyclerViewProductSales.ViewHolder>(){
    lateinit var binding: RecyclerItemBinding

    inner class ViewHolder(val binding: RecyclerItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater,R.layout.recycler_item,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.run {
            stock.visibility = View.GONE
            stockT.visibility = View.GONE
            freeTitle.visibility = View.GONE
            itemCountHandler.visibility = View.GONE
            freeCountHandler.visibility = View.GONE
            sunInfo.text = "Sold Quantity :"
            name.text = list[position].name
            ordersCount.text = list[position].info.toString()
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}
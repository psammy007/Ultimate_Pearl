package com.example.ultimatepearl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ultimatepearl.databinding.RecyclerItemBinding

class RecyclerViewReturnItemAdapter(private var list: MutableList<OrderItem>,):
    RecyclerView.Adapter<RecyclerViewReturnItemAdapter.ViewHolder>(){

    private lateinit var binding: RecyclerItemBinding

    inner class ViewHolder(val binding: RecyclerItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater,R.layout.recycler_item,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = list[position].product.name
        holder.binding.sunInfo.text = "MRP:"
        holder.binding.ordersCount.text = list[position].product.mrp.toString()
        holder.binding.count.text = list[position].count.toString()
        holder.binding.freeCountHandler.visibility = View.GONE
        holder.binding.stock.visibility = View.GONE
        holder.binding.stockT.visibility = View.GONE
        holder.binding.run {
            increment.setOnClickListener {
                list[position].count  = list[position].count + 1
                count.text = list[position].count.toString()
            }

            decrement.setOnClickListener {
                if(list[position].count>0){
                    list[position].count  = list[position].count - 1
                    count.text = list[position].count.toString()
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}
package com.example.ultimatepearl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ultimatepearl.databinding.RecyclerItemBinding

class RecyclerViewMastersAdapter(private val list:MutableList<MasterTemp>, private val eventHandler: ClickEventHandler):RecyclerView.Adapter<RecyclerViewMastersAdapter.ViewHolder>() {

    lateinit var binding:RecyclerItemBinding

    inner class ViewHolder(val binding:RecyclerItemBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater,R.layout.recycler_item,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = list[position].name
        holder.binding.ordersCount.text = list[position].orderCount.toString()
        holder.binding.sunInfo.text = "Orders :"
        holder.binding.increment.visibility = View.GONE
        holder.binding.decrement.visibility = View.GONE
        holder.binding.count.visibility = View.GONE
        holder.binding.freeTitle.visibility = View.GONE
        holder.binding.freeCountHandler.visibility = View.GONE
        holder.binding.stock.visibility = View.GONE
        holder.binding.stockT.visibility = View.GONE
        holder.binding.name.setOnClickListener {
            eventHandler.openInDetail(list[position].id)
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}
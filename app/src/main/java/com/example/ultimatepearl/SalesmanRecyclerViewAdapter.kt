package com.example.ultimatepearl

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ultimatepearl.databinding.RecyclerItemBinding

class SalesmanRecyclerViewAdapter(private val list: MutableList<Salesman>, private val eventHandler: ClickEventHandler):
RecyclerView.Adapter<SalesmanRecyclerViewAdapter.ViewHolder>(){

    private lateinit var binding: RecyclerItemBinding

    inner class ViewHolder(val binding: RecyclerItemBinding):RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SalesmanRecyclerViewAdapter.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater,R.layout.recycler_item,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SalesmanRecyclerViewAdapter.ViewHolder, position: Int) {

        holder.binding.run {
            name.text = list[position].name
            sunInfo.text = "Contact :"
            ordersCount.text = list[position].contact
            increment.visibility = View.GONE
            decrement.visibility = View.GONE
            count.visibility = View.GONE
            holder.binding.freeTitle.visibility = View.GONE
            holder.binding.freeCountHandler.visibility = View.GONE
            name.setOnClickListener {
                eventHandler.openInDetail(list[position].id)
            }
        }
    }

    override fun getItemCount(): Int {
        return list.size
    }

}
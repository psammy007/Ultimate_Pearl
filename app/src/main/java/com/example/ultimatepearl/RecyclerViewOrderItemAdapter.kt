package com.example.ultimatepearl

import android.content.res.Resources
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.RecyclerView
import com.example.ultimatepearl.databinding.RecyclerItemBinding

class RecyclerViewOrderItemAdapter(private var list: MutableList<OrderItem>,private val countHandler: AmountUpdater):
    RecyclerView.Adapter<RecyclerViewOrderItemAdapter.ViewHolder>(){

    private lateinit var binding: RecyclerItemBinding

    inner class ViewHolder(val binding: RecyclerItemBinding): RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        binding = DataBindingUtil.inflate(inflater,R.layout.recycler_item,parent,false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.binding.name.text = list[position].product.name
        holder.binding.sunInfo.text = "PTR :"
        holder.binding.count.text = list[position].count.toString()
        holder.binding.freeCount.text = list[position].freeCount.toString()
        holder.binding.ordersCount.text = "\u20B9" + " " + list[position].product.ptrRate.toString()
        holder.binding.stock.visibility = View.GONE
        holder.binding.stockT.visibility = View.GONE
        if(list[position].product.inStock>0){
            holder.binding.increment.setOnClickListener {
                list[position].count  = list[position].count + 1
                if(list[position].freeCount+list[position].count>=list[position].product.inStock){
                    holder.binding.freeIncrement.isClickable = false
                    holder.binding.increment.isClickable = false
                }
                holder.binding.count.text = list[position].count.toString()
                var amount = (list[position].product.ptrRate * (100 - list[position].product.discount)) / 100
                amount = String.format("%.2f", amount).toFloat()
                countHandler.updateAmount(amount,true,list[position].product.id)
            }

            holder.binding.freeIncrement.setOnClickListener {
                list[position].freeCount += 1
                holder.binding.freeCount.text = list[position].freeCount.toString()
                if(list[position].freeCount+list[position].count>=list[position].product.inStock){
                    holder.binding.freeIncrement.isClickable = false
                    holder.binding.increment.isClickable = false
                }
            }

            holder.binding.freeDecrement.setOnClickListener {
                if(list[position].freeCount > 0){
                    list[position].freeCount -= 1
                    holder.binding.freeCount.text = list[position].freeCount.toString()
                    if(list[position].freeCount+list[position].count<list[position].product.inStock){
                        holder.binding.freeIncrement.isClickable = true
                        holder.binding.increment.isClickable = true
                    }
                }
            }

            holder.binding.decrement.setOnClickListener {
                if(list[position].count > 0){
                    list[position].count  = list[position].count - 1
                    if(list[position].freeCount+list[position].count<list[position].product.inStock){
                        holder.binding.freeIncrement.isClickable = true
                        holder.binding.increment.isClickable = true
                    }
                    holder.binding.count.text = list[position].count.toString()
                    var amount = (list[position].product.ptrRate * (100 - list[position].product.discount)) / 100
                    amount = String.format("%.2f", amount).toFloat()
                    countHandler.updateAmount(amount,false,list[position].product.id)
                }
            }
        }else{
            holder.binding.name.setTextColor(Color.parseColor("#848884"))
        }

        val editText = binding.count
        editText.showSoftInputOnFocus = false

    }

    override fun getItemCount(): Int {
        return list.size
    }

}
package com.example.ultimatepearl

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ultimatepearl.databinding.FragmentReturnsSummaryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ReturnsSummaryFragment : Fragment() {

    private lateinit var binding:FragmentReturnsSummaryBinding
    private lateinit var args:ReturnsSummaryFragmentArgs
    private lateinit var databaseRef: DatabaseReference
    private lateinit var itemsList: MutableList<OrderItem>
    private lateinit var adapter: RecyclerViewReturnItemDetailAdapter
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_returns_summary,container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initializeVariables()
        getReturnDetails()
        setBackAction()
    }

    private fun initializeVariables() {
        args = ReturnsSummaryFragmentArgs.fromBundle(requireArguments())
        itemsList = ArrayList()
        databaseRef = FirebaseDatabase.getInstance().getReference(userId).child("Returns").child(args.masterId).child(args.orderId)
        adapter = RecyclerViewReturnItemDetailAdapter(itemsList,false)
        val itemList = binding.itemsList
        itemList.layoutManager = GridLayoutManager(activity,1)
        itemList.adapter = adapter
        (activity as DrawerAction).lockDrawer()
    }

    private fun getReturnDetails(){
        databaseRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val key = snapshot.key.toString()
                val salesman = snapshot.child("salesman").child("name").value.toString()
                val items = snapshot.child("items")
                updateAdapterData(items)
                binding.returnId.text = key
                binding.salesman.text = salesman

            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun updateAdapterData(items: DataSnapshot){
        itemsList.clear()
        for (item in items.children){
            Log.d("item",item.toString())
            val count = item.child("count").value.toString().toInt()
            val freeCount = item.child("freeCount").value.toString().toInt()
            val id = item.child("product").child("id").value.toString()
            val mrp = item.child("product").child("mrp").value.toString().toFloat()
            val name = item.child("product").child("name").value.toString()
            val groupId = item.child("product").child("productGroup").child("id").value.toString()
            val groupName = item.child("product").child("productGroup").child("group").value.toString()
            val hsnCode =  item.child("product").child("hsnCode").value.toString()
            val ptrRate = item.child("product").child("ptrRate").value.toString().toFloat()
            val taxRate = item.child("product").child("taxRate").value.toString().toFloat()
            val stock = item.child("product").child("inStock").value.toString()
            val base = item.child("product").child("scheme").child("base").value.toString()
            val free = item.child("product").child("scheme").child("free").value.toString()
            val discount =item.child("product").child("discount").value.toString()
            val soldQuantity = item.child("product").child("soldQuantity").value.toString().toLong()
            val orderItem = OrderItem(Product(id,
                name,
                ProductGroup(groupId,groupName),
                ptrRate,
                mrp,
                taxRate,
                hsnCode,
                stock.toInt(),
                Scheme(base.toInt(),free.toInt()),
                discount.toFloat(),soldQuantity),
                count,freeCount)
            itemsList.add(0,orderItem)
        }
        adapter.notifyDataSetChanged()
    }

    private fun setBackAction(){
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

}
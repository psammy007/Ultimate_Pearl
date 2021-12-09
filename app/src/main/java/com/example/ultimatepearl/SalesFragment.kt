package com.example.ultimatepearl

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ultimatepearl.databinding.FragmentSalesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class SalesFragment : Fragment() {

    private lateinit var binding:FragmentSalesBinding
    private lateinit var salesmanRef : DatabaseReference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private lateinit var list:MutableList<Sample>
    private lateinit var results:MutableList<Sample>
    private lateinit var spinnerAdapter: ArrayAdapter<Sample>
    private lateinit var adapter:RecyclerViewProductSales
    private lateinit var orderRef:DatabaseReference
    var rawResult = mutableMapOf<String, Int>().withDefault { 0 }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_sales,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        fetchSalesman()
    }

    private fun initializeVariables() {
        list = ArrayList()
        results = ArrayList()
        list.add(0,Sample("","--Select salesman--",""))
        spinnerAdapter = ArrayAdapter(requireContext(),R.layout.support_simple_spinner_dropdown_item,list)
        val spinnerView = binding.salesSpinner
        spinnerView.adapter = spinnerAdapter
        salesmanRef = FirebaseDatabase.getInstance().getReference(userId).child("Salesmans")
        orderRef = FirebaseDatabase.getInstance().getReference(userId).child("Orders")
        adapter = RecyclerViewProductSales(results)
        val recyclerView = binding.productList
        recyclerView.layoutManager = GridLayoutManager(activity,1)
        recyclerView.adapter = adapter
    }

    private fun fetchSalesman(){
        salesmanRef.get().addOnSuccessListener {  snapshot ->
            for(postSnapshot in snapshot.children){
                val id = postSnapshot.key.toString()
                val name = postSnapshot.child("name").value.toString()
                val sample = Sample(id,name,"")
                list.add(1,sample)
            }
            spinnerAdapter.notifyDataSetChanged()
            if(list.size==1){
                binding.loading.visibility = View.GONE
                binding.msg.text = "No salesman yet"
            }else{
                setSpinnerActions()
            }
        }
    }

    private fun setSpinnerActions() {
        val spinnerView = binding.salesSpinner
        spinnerView.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if(position==0)
                    clearData()
                else {
                    binding.msg.visibility = View.GONE
                    binding.productList.visibility = View.GONE
                    binding.loading.visibility = View.VISIBLE
                    fetchData(list[position])
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }

    private fun clearData(){
        results.clear()
        adapter.notifyDataSetChanged()
        binding.msg.visibility = View.VISIBLE
        binding.msg.text = "No salesman selected"

    }

    private fun fetchData(sample:Sample){
        results.clear()
        rawResult.clear()
        orderRef.get().addOnSuccessListener{ snapshot ->
            for(postSnapshot in snapshot.children){
                for(order in postSnapshot.children){
                    if(order.child("salesman").child("name").value.toString()==sample.name){
                        extractItems(order.child("items"))
                    }
                }
            }
            extractData(sample.name)
        }
    }

    private fun extractItems(snapshot: DataSnapshot){
        for(postSnapshot in snapshot.children){
            Log.d("post",postSnapshot.toString())
            val key = postSnapshot.child("product").child("name").value.toString()
            val subInfo = postSnapshot.child("count").value.toString().toInt() + postSnapshot.child("freeCount").value.toString().toInt()
            val sum = rawResult.getOrDefault(key,0) + subInfo
            rawResult[key] = sum
        }
    }

    private fun extractData(name:String){
        for(key in rawResult.keys){
            results.add(0,Sample("",key,rawResult[key].toString()))
        }
        updateUI(name)
    }

    private fun updateUI(name:String){
        adapter.notifyDataSetChanged()
        binding.loading.visibility = View.GONE
        if(results.isEmpty()){
            binding.msg.text = "No data related to $name"
            binding.productList.visibility = View.GONE
            binding.msg.visibility = View.VISIBLE
        }else{
            binding.msg.visibility = View.GONE
            binding.productList.visibility = View.VISIBLE
        }
    }
}
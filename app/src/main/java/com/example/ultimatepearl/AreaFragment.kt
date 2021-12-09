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
import com.example.ultimatepearl.databinding.FragmentAreaBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class AreaFragment : Fragment() {

    private lateinit var binding:FragmentAreaBinding
    private lateinit var areaRef :DatabaseReference
    private val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private lateinit var list:MutableList<Sample>
    private lateinit var results:MutableList<Sample>
    private lateinit var masters:MutableList<Sample>
    private lateinit var spinnerAdapter: ArrayAdapter<Sample>
    private lateinit var adapter:RecyclerViewProductSales
    private lateinit var orderRef:DatabaseReference
    var rawResult = mutableMapOf<String, Int>().withDefault { 0 }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_area,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        fetchAreas()
        fetchMasters()
    }

    private fun initializeVariables() {
        list = ArrayList()
        results = ArrayList()
        masters = ArrayList()
        list.add(0,Sample("","--Select area--",""))
        spinnerAdapter = ArrayAdapter(requireContext(),R.layout.support_simple_spinner_dropdown_item,list)
        val spinnerView = binding.areaSpinner
        spinnerView.adapter = spinnerAdapter
        areaRef = FirebaseDatabase.getInstance().getReference(userId).child("Areas")
        orderRef = FirebaseDatabase.getInstance().getReference(userId).child("Orders")
        adapter = RecyclerViewProductSales(results)
        val recyclerView = binding.productList
        recyclerView.layoutManager = GridLayoutManager(activity,1)
        recyclerView.adapter = adapter
    }

    private fun fetchAreas(){
        areaRef.get().addOnSuccessListener {  snapshot ->
            for(postSnapshot in snapshot.children){
                val id = postSnapshot.key.toString()
                val name = postSnapshot.child("area").value.toString()
                val sample = Sample(id,name,"")
                list.add(1,sample)
            }
            spinnerAdapter.notifyDataSetChanged()
            if(list.size==1){
                binding.loading.visibility = View.GONE
                binding.msg.text = "No masters yet"
            }else{
                setSpinnerActions()
            }
        }
    }

    private fun fetchMasters(){
        FirebaseDatabase.getInstance().getReference(userId).child("Masters").get().addOnSuccessListener {  snapshot ->
            masters.clear()
            for(postSnapshot in snapshot.children){
                val id = postSnapshot.key.toString()
                val name = postSnapshot.child("name").value.toString()
                val area = postSnapshot.child("area").child("area").value.toString()
                val sample = Sample(id,name,area)
                masters.add(0,sample)
            }
        }
    }

    private fun setSpinnerActions() {
        val spinnerView = binding.areaSpinner
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
        binding.msg.text = "No area selected"

    }

    private fun fetchData(sample:Sample){
        rawResult.clear()
        results.clear()
        var areaWiseMasters = ArrayList<Sample>()
        for(master in masters){
            if(master.info==sample.name)
                areaWiseMasters.add(0,master)
        }
        groupData(areaWiseMasters,sample.name)
    }


    private fun groupData(areaWiseMaster: ArrayList<Sample>,name:String){
        val length = areaWiseMaster.size
        if(length>0) {
            for (i in areaWiseMaster.indices) {
                orderRef.child(areaWiseMaster[i].id).get().addOnSuccessListener { snapshot ->
                    for (postSnapshot in snapshot.children) {
                        for (item in postSnapshot.child("items").children) {
                            val key = item.child("product").child("name").value.toString()
                            val count = item.child("count").value.toString()
                                .toInt() + item.child("freeCount").value.toString().toInt()
                            var sum = rawResult.getOrDefault(key, 0)
                            rawResult[key] = sum + count
                        }
                    }
                    if (i == (length - 1))
                        extractData(name)
                }
            }
        }else
            updateUI(name)
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
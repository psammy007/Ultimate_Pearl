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
import com.example.ultimatepearl.databinding.FragmentStockBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class StockFragment : Fragment() {

    private lateinit var binding:FragmentStockBinding
    private lateinit var groupRef :DatabaseReference
    private lateinit var list: MutableList<Sample>
    private val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private lateinit var results:MutableList<Sample>
    private lateinit var adapterData:MutableList<Sample>
    private lateinit var spinnerAdapter: ArrayAdapter<Sample>
    private lateinit var adapter:RecyclerViewProductSales

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_stock,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d("testing","on create view")
        initializeVariables()
        fetchProductGroups()

    }

    private fun initializeVariables() {
        list = ArrayList()
        results = ArrayList()
        adapterData = ArrayList()
        list.add(0,Sample("","--Select group--",""))
        spinnerAdapter = ArrayAdapter(requireContext(),R.layout.support_simple_spinner_dropdown_item,list)
        val spinnerView = binding.groupSpinner
        spinnerView.adapter = spinnerAdapter
        groupRef = FirebaseDatabase.getInstance().getReference(userId).child("ProductGroups")
        adapter = RecyclerViewProductSales(adapterData)
        val productView = binding.productSales
        productView.adapter = adapter
        productView.layoutManager = GridLayoutManager(activity,1)
    }

    private fun fetchProductGroups() {
        groupRef.get().addOnSuccessListener { snapshot ->
            for (postSnapshot in snapshot.children) {
                val id = postSnapshot.key.toString()
                val name = postSnapshot.child("group").value.toString()
                val sample = Sample(id, name, "")
                list.add(1, sample)
            }
            if (list.size == 1){
                binding.loading.visibility = View.GONE
                binding.msg.text = "No groups yet"
            }else {
                fetchProducts()
                setSpinnerActions()
            }
        }
    }


    private fun setSpinnerActions() {
        val spinnerView = binding.groupSpinner
        spinnerView.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if(position==0) {
                    clearData("No group selected")
                } else {
                    binding.productSales.visibility = View.GONE
                    binding.loading.visibility = View.VISIBLE
                    filterData(list[position])
                }
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }

    private fun clearData(msg:String){
        adapterData.clear()
        adapter.notifyDataSetChanged()
        binding.msg.visibility = View.VISIBLE
        binding.msg.text = msg

    }

    private fun filterData(sample:Sample){
        adapterData.clear()
        for(product in results){
            if(product.id==sample.name)
                adapterData.add(product)
        }
        updateUI(sample.name)
    }

    private fun updateUI(group:String){
        adapter.notifyDataSetChanged()
        binding.loading.visibility = View.GONE
        if(adapterData.isEmpty()){
            binding.msg.visibility = View.VISIBLE
            binding.msg.text = "No product from $group"
            binding.productSales.visibility = View.GONE
        }else{
            binding.msg.visibility = View.GONE
            binding.productSales.visibility = View.VISIBLE
        }
    }

    private fun fetchProducts(){
        FirebaseDatabase.getInstance().getReference(userId).child("Products").addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    val id = postSnapshot.child("productGroup").child("group").value.toString()
                    val name = postSnapshot.child("name").value.toString()
                    val qun = postSnapshot.child("soldQuantity").value.toString()
                    val sample = Sample(id,name,qun)
                    results.add(0,sample)
                }
                binding.loading.visibility = View.GONE
                binding.msg.text = "No group selected"
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

}

package com.example.ultimatepearl

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ultimatepearl.databinding.FragmentAddReturnsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class AddReturnsFragment : Fragment(){

    private lateinit var binding:FragmentAddReturnsBinding
    private lateinit var orderItemList: MutableList<OrderItem>
    private lateinit var adapterData: MutableList<OrderItem>
    private lateinit var salesmanList: MutableList<Salesman>
    private lateinit var databaseProductRef: DatabaseReference
    private lateinit var databaseSalesmanRef: DatabaseReference
    private lateinit var databaseReturnReference: DatabaseReference
    private lateinit var adapter: RecyclerViewReturnItemAdapter
    private lateinit var orderedItems:MutableList<OrderItem>
    private lateinit var args: AddReturnsFragmentArgs
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var generatePattern:String
    private lateinit var salesman:Salesman


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_add_returns,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        getData()
        setSalesman()
        setSaveAction()
        setBackAction()
    }

    private fun getData(){
        databaseProductRef.addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                this@AddReturnsFragment.orderItemList.clear()
                for(postSnapshot in snapshot.children){
                    val id = postSnapshot.key.toString()
                    val name = postSnapshot.child("name").value.toString()
                    val groupId = postSnapshot.child("productGroup").child("id").value.toString()
                    val groupName = postSnapshot.child("productGroup").child("group").value.toString()
                    val ptrRate = postSnapshot.child("ptrRate").value.toString().toFloat()
                    val mrp = postSnapshot.child("mrp").value.toString().toFloat()
                    val taxRate = postSnapshot.child("taxRate").value.toString().toFloat()
                    val hsnCode = postSnapshot.child("hsnCode").value.toString()
                    val stock = postSnapshot.child("inStock").value.toString()
                    val base = postSnapshot.child("scheme").child("base").value.toString()
                    val free = postSnapshot.child("scheme").child("free").value.toString()
                    val discount = postSnapshot.child("discount").value.toString()
                    val soldQuantity = postSnapshot.child("soldQuantity").value.toString().toLong()
                    val orderItem = OrderItem(Product(id,name,ProductGroup(groupId,groupName), ptrRate, mrp, taxRate,hsnCode,stock.toInt(), Scheme(base.toInt(),free.toInt()),discount.toFloat(),soldQuantity),0,0)
                    this@AddReturnsFragment.orderItemList.add(0,orderItem)
                }
                this@AddReturnsFragment.updateData()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })
    }

    private fun setSalesman(){
        databaseSalesmanRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    val id = postSnapshot.key.toString()
                    val name = postSnapshot.child("name").value.toString()
                    val contact = postSnapshot.child("contact").value.toString()
                    val salesman = Salesman(id, name, contact)
                    salesmanList.add(1,salesman)
                }
                setSpinner()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("database error",error.message)
            }

        })
    }

    private fun setSaveAction(){
        binding.saveReturn.setOnClickListener {
            orderedItems.clear()
            for(item in orderItemList){
                if(item.count > 0)
                    orderedItems.add(0,item)
            }
            when {
                orderedItems.isEmpty() -> Toast.makeText(requireContext(),"No product selected",Toast.LENGTH_SHORT).show()
                salesman.name == "--Select Salesman--" -> Toast.makeText(requireContext(),"Select salesman",Toast.LENGTH_SHORT).show()
                else -> {
                    binding.saving.visibility = View.VISIBLE
                    binding.saveReturn.isClickable = false
                    FirebaseDatabase.getInstance().getReference(userId).child("Masters").child(args.masterId).child("returnCount").get().addOnSuccessListener {
                        val key = it.value.toString()
                        Log.d("test",it.toString())
                        val returns = Return(key,orderedItems,getTimeStamp(),salesman)
                        databaseReturnReference.child(key).setValue(returns).addOnSuccessListener {
                            FirebaseDatabase.getInstance().getReference(userId).child("Masters").child(args.masterId).child("returnCount").setValue(key.toInt()+1).addOnSuccessListener {
                                view?.findNavController()?.navigateUp()
                            }
                        }

                    }
                }
            }
        }
    }

    private fun setSpinner(){
        val salesmanAdapter = ArrayAdapter(requireContext(),R.layout.support_simple_spinner_dropdown_item,salesmanList)
        val spinner = binding.salesmanSpinner
        spinner.adapter = salesmanAdapter
        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, p2: Int, p3: Long) {
                salesman = salesmanList[p2]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }
        }
    }

    private fun updateData(){
        orderItemList.sortBy { it.product.name }
        adapterData.clear()
        adapterData.addAll(orderItemList)
        adapter.notifyDataSetChanged()
        updateUI("No Products.\nTap + to add product")
    }

    private fun initializeVariables() {
        orderItemList = ArrayList()
        adapterData = ArrayList()
        orderedItems = ArrayList()
        salesmanList = ArrayList()
        salesmanList.add(0, Salesman("","--Select Salesman--",""))
        args = AddReturnsFragmentArgs.fromBundle(requireArguments())
        databaseProductRef = FirebaseDatabase.getInstance().getReference(userId).child("Products")
        databaseSalesmanRef = FirebaseDatabase.getInstance().getReference(userId).child("Salesmans")
        databaseReturnReference = FirebaseDatabase.getInstance().getReference(userId).child("Returns").child(args.masterId)
        adapter = RecyclerViewReturnItemAdapter(adapterData)
        val orderItemsList = binding.orderItemList
        orderItemsList.layoutManager = GridLayoutManager(activity,1)
        orderItemsList.adapter = adapter
        implementSearch()
        (activity as DrawerAction).lockDrawer()
    }

    private fun setBackAction(){
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }


    private fun updateUI(msg:String){
        binding.loading.visibility = View.GONE
        if(adapterData.isEmpty()){
            binding.orderItemList.visibility = View.GONE
            binding.message.text = msg
            binding.message.visibility = View.VISIBLE
        }else{
            binding.searchBar.visibility = View.VISIBLE
            binding.message.visibility = View.GONE
            binding.orderItemList.visibility = View.VISIBLE
        }
    }

    private fun implementSearch(){
        binding.searchBar.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(p0 != null){
                    generatePattern = "^"
                    for(i in p0){
                        generatePattern += when{
                            i.isLetter() -> "[${i.lowercase() + i.uppercase()}]"
                            else -> "[$i]"
                        }
                    }
                    val pattern = Regex(generatePattern)
                    val searchResult = ArrayList<OrderItem>()
                    for(orderItem in orderItemList){
                        if(pattern.containsMatchIn(orderItem.product.name)){
                            searchResult.add(0,orderItem)
                        }
                    }
                    adapterData.clear()
                    adapterData.addAll(searchResult.sortedBy { it.product.name })
                    adapter.notifyDataSetChanged()
                    if(searchResult.size==0){
                        updateUI("No Match")
                    }else{
                        updateUI("")
                        adapter.notifyDataSetChanged()
                    }
                }else{
                    adapterData.clear()
                    adapterData.addAll(orderItemList)
                    adapter.notifyDataSetChanged()
                    updateUI("No products\nTap + to create product")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

    private fun getTimeStamp(): String {
        val time = Calendar.getInstance()
        return time.get(Calendar.DAY_OF_MONTH).toString() + "-" + time.get(Calendar.MONTH)
            .toString() + "-" + time.get(Calendar.YEAR).toString() + "/" + time.get(Calendar.HOUR_OF_DAY)
            .toString() + ":" + time.get(Calendar.MINUTE).toString()
    }

}
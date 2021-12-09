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
import com.example.ultimatepearl.databinding.FragmentOrderItemsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*
import kotlin.collections.ArrayList

class OrderItemsFragment : Fragment(),AmountUpdater,ClickEventHandler {

    private lateinit var binding:FragmentOrderItemsBinding
    private lateinit var orderItemList: MutableList<OrderItem>
    private lateinit var adapterData: MutableList<OrderItem>
    private lateinit var salesmanList: MutableList<Salesman>
    private lateinit var databaseProductRef: DatabaseReference
    private lateinit var databaseSalesmanRef: DatabaseReference
    private lateinit var adapter: RecyclerViewOrderItemAdapter
    private lateinit var orderedItems:MutableList<OrderItem>
    private lateinit var args: OrderItemsFragmentArgs
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var generatePattern:String
    private lateinit var salesman:Salesman

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_order_items,container,false)
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

    private fun initializeVariables() {
        orderItemList = ArrayList()
        adapterData = ArrayList()
        orderedItems = ArrayList()
        salesmanList = ArrayList()
        salesmanList.add(0, Salesman("","--Select Salesman--",""))
        args = OrderItemsFragmentArgs.fromBundle(requireArguments())
        databaseProductRef = FirebaseDatabase.getInstance().getReference(userId).child("Products")
        databaseSalesmanRef = FirebaseDatabase.getInstance().getReference(userId).child("Salesmans")
        adapter = RecyclerViewOrderItemAdapter(adapterData,this)
        val orderItemsList = binding.orderItemList
        orderItemsList.layoutManager = GridLayoutManager(activity,1)
        orderItemsList.adapter = adapter
        implementSearch()
        (activity as DrawerAction).lockDrawer()
    }

    private fun getData(){
        databaseProductRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                this@OrderItemsFragment.orderItemList.clear()
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
                    this@OrderItemsFragment.orderItemList.add(0,orderItem)
                }
                this@OrderItemsFragment.updateData()
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

    private fun setSpinner(){
        val salesmanAdapter = ArrayAdapter(requireContext(),R.layout.support_simple_spinner_dropdown_item,salesmanList)
        val spinner = binding.salesmanSpinner
        spinner.adapter = salesmanAdapter
        spinner.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
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

    override fun updateAmount(amountDiff: Float,add:Boolean,itemId:String) {
        var price = binding.amount.text.toString()
        price = price.replace(" \u20B9","")
        var amount = price.toFloat()
        if(add)
            amount += amountDiff
        else
            amount -= amountDiff
        amount = String.format("%.2f",amount).toFloat()
        Log.d("amount update",amount.toString())
        binding.amount.text = "$amount \u20B9"
    }


    private fun setSaveAction(){
        binding.save.setOnClickListener {
            orderedItems.clear()
            for(item in orderItemList){
                if(item.count > 0)
                    orderedItems.add(0,item)
            }
            val totalAmount = binding.amount.text.toString().replace(" \u20B9","").toFloat()
            saveOrder(orderedItems,totalAmount,salesman)
        }
    }


    private fun saveOrder(items:MutableList<OrderItem>,totalAmount:Float,salesman: Salesman){
        when {
            items.isEmpty() -> {
                Toast.makeText(requireContext(),"No products selected",Toast.LENGTH_SHORT).show()
            }
            salesman.name=="--Select Salesman--" -> {
                Toast.makeText(requireContext(),"Select salesman",Toast.LENGTH_SHORT).show()
            }
            else -> {
                val orderDetail = OrderDetailFragment(items,totalAmount,this,args.masterId,salesman)
                orderDetail.show(parentFragmentManager,"Order Detail")
            }
        }
    }


    private fun setBackAction(){
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

    override fun openInDetail(Id: String) {
        view?.findNavController()?.navigateUp()
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

}
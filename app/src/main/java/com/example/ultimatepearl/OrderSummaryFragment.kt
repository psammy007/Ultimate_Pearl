package com.example.ultimatepearl

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ultimatepearl.databinding.FragmentOrderSummaryBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.apache.poi.ss.formula.functions.Value

class OrderSummaryFragment : Fragment() {

    private lateinit var binding:FragmentOrderSummaryBinding
    private lateinit var args: OrderSummaryFragmentArgs
    private lateinit var databaseRef: DatabaseReference
    private var flag = true
    var bill = ""
    private lateinit var itemsList: MutableList<OrderItem>
    private lateinit var adapter: RecyclerViewOrderItemDetailAdapter
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_order_summary,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        getOrderDetails()
        setBackAction()
        setCheckBoxHandler()
    }

    private fun initializeVariables() {
        itemsList = ArrayList()
        args = OrderSummaryFragmentArgs.fromBundle(requireArguments())
        databaseRef = FirebaseDatabase.getInstance().getReference(userId).child("Orders").child(args.masterId).child(args.orderId)
        adapter = RecyclerViewOrderItemDetailAdapter(itemsList,false)
        val itemList = binding.itemsList
        itemList.layoutManager = GridLayoutManager(activity,1)
        itemList.adapter = adapter
        (activity as DrawerAction).lockDrawer()
    }

    private fun getOrderDetails(){
        databaseRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val key = snapshot.key.toString()
                bill = String.format("%.2f",snapshot.child("bill").value.toString().toFloat())
                val status = snapshot.child("paymentStatus").value.toString().toBoolean()
                val salesman = snapshot.child("salesman").child("name").value.toString()
                val items = snapshot.child("items")
                updateAdapterData(items)
                val timeStamp = snapshot.child("timeStamp").value.toString().split("/")
                val date = timeStamp[0]
                val time = timeStamp[1]
                binding.orderDate.text = date
                binding.orderTime.text = time
                binding.orderId.text = key
                binding.salesman.text = salesman
                binding.totalAmount.text = "$bill \u20B9"
                if(status && flag){
                    val checkBox = binding.paymentStatus
                    checkBox.isChecked = true
                    checkBox.isEnabled = false
                    flag = false
                    binding.paymentIcon.setBackgroundResource(R.drawable.ic_success)
                }else{
                    binding.paymentIcon.setBackgroundResource(R.drawable.ic_unpaid)
                }
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun setBackAction(){
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

    private fun setCheckBoxHandler(){
        binding.paymentStatus.setOnClickListener {
            databaseRef.child("paymentStatus").setValue(binding.paymentStatus.isChecked)
            updateDetails()
            updateInMaster()
            if(binding.paymentStatus.isChecked){
                binding.paymentIcon.setBackgroundResource(R.drawable.ic_success)
                binding.paymentStatus.isEnabled = false
            }else{
                binding.paymentIcon.setBackgroundResource(R.drawable.ic_unpaid)
            }
        }
    }

    private fun updateInMaster(){
        FirebaseDatabase.getInstance().getReference(userId).child("Masters").child(args.masterId).child("unpaidBill").get().addOnSuccessListener {
            var amount = it.value.toString().toFloat()
            amount -= binding.totalAmount.text.toString().replace("₹","").toFloat()
            FirebaseDatabase.getInstance().getReference(userId).child("Masters").child(args.masterId).child("unpaidBill").setValue(amount).addOnSuccessListener {
                Toast.makeText(requireContext(),"Amount updated",Toast.LENGTH_SHORT).show()
            }

        }
    }

    private fun updateDetails() {
        FirebaseDatabase.getInstance().getReference(userId).child("OrderDetails").child("unPaidBill").addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var unPaidBill = snapshot.value.toString().toFloat()
                unPaidBill -= bill.toFloat()
                FirebaseDatabase.getInstance().getReference(userId).child("OrderDetails").child("unPaidBill").setValue(unPaidBill).addOnSuccessListener {
                    Toast.makeText(requireContext(),"Payment status updated",Toast.LENGTH_SHORT).show()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })
    }

    private fun updateAdapterData(items:DataSnapshot){
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

}
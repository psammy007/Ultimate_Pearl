package com.example.ultimatepearl

import android.app.DatePickerDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ultimatepearl.databinding.FragmentOrderDetailBinding
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import java.util.*

class OrderDetailFragment(private val list: MutableList<OrderItem>,private val totalAmount:Float,private val clickEventHandler: ClickEventHandler,private val masterId:String,val salesman: Salesman) : BottomSheetDialogFragment(),
    DatePickerDialog.OnDateSetListener {

    private lateinit var binding:FragmentOrderDetailBinding
    private lateinit var adapter: RecyclerViewOrderItemDetailAdapter
    private lateinit var databaseOrderRef: DatabaseReference
    private lateinit var databaseMasterRef: DatabaseReference
    private lateinit var databaseOrdersDetailRef: DatabaseReference
    private lateinit var databaseProductRef: DatabaseReference
    var orderId = 0
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_order_detail,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        setCurrentDate()
        setUpCalendar()
        setUpSaveOrderAction()
    }

    private fun setCurrentDate() {
        val calender = Calendar.getInstance()
        val year = calender.get(Calendar.YEAR)
        val month = calender.get(Calendar.MONTH)
        val day = calender.get(Calendar.DAY_OF_MONTH)
        binding.date.setText("$day-${month+1}-$year")
    }

    private fun initializeVariables(){
        adapter = RecyclerViewOrderItemDetailAdapter(list,true)
        val orderItemView = binding.orderItems
        binding.totalAmount.text = "$totalAmount \u20B9"
        databaseOrderRef = FirebaseDatabase.getInstance().getReference(userId).child("Orders").child(masterId)
        databaseMasterRef = FirebaseDatabase.getInstance().getReference(userId).child("Masters")
        databaseOrdersDetailRef = FirebaseDatabase.getInstance().getReference(userId).child("OrderDetails")
        databaseProductRef = FirebaseDatabase.getInstance().getReference(userId).child("Products")
        orderItemView.layoutManager = GridLayoutManager(activity,1)
        orderItemView.adapter = adapter
        binding.salesmanName.text = salesman.name
        databaseMasterRef.child(masterId).child("orderCount").addListenerForSingleValueEvent(
            object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    orderId = snapshot.value.toString().toInt()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Database error",error.message)
                }

            }
        )
    }

    private fun setUpSaveOrderAction(){
        binding.saveOrder.setOnClickListener {
            binding.saveOrder.isClickable = false
            val paymentStatus = binding.paymentStatus.isChecked
            binding.loading.visibility = View.VISIBLE
            incrementOrderCount(paymentStatus)
            saveOrder(paymentStatus)
        }
    }

    private fun setUpCalendar(){
        binding.calendar.setOnClickListener {
            val calender = Calendar.getInstance()
            val year = calender.get(Calendar.YEAR)
            val month = calender.get(Calendar.MONTH)
            val day = calender.get(Calendar.DAY_OF_MONTH)
            DatePickerDialog(requireContext(), this, year, month, day).show()
        }
    }

    private fun incrementOrderCount(isPaid:Boolean) {
        databaseOrdersDetailRef.addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                var orderCount = snapshot.child("TotalOrders").value.toString().toInt()
                var grossAmount = snapshot.child("TotalAmount").value.toString().toFloat()
                orderCount += 1
                grossAmount += totalAmount
                grossAmount = String.format("%.2f",grossAmount).toFloat()
                if(!isPaid){
                    var unPaid = snapshot.child("unPaidBill").value.toString().toFloat()
                    unPaid += totalAmount
                    unPaid = String.format("%.2f",unPaid).toFloat()
                    databaseOrdersDetailRef.child("unPaidBill").setValue(unPaid)
                }
                databaseOrdersDetailRef.child("TotalOrders").setValue(orderCount)
                databaseOrdersDetailRef.child("TotalAmount").setValue(grossAmount)

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })
    }

    private fun saveOrder(paymentStatus:Boolean){
        val key = (orderId + 1).toString()
        val order = Order(key,list,String.format("%.2f",totalAmount).toFloat(),getTimeStamp(),paymentStatus,salesman)
        databaseOrderRef.child(key).setValue(order).addOnSuccessListener {
            Toast.makeText(requireContext(),"Order saved successfully",Toast.LENGTH_SHORT).show()
            updateOrderDetails(paymentStatus,totalAmount)
            updateInStocks()
            dismiss()
            clickEventHandler.openInDetail("0")
        }.addOnFailureListener{
            binding.saveOrder.isClickable = true
            binding.loading.visibility = View.GONE
            Toast.makeText(requireContext(),"Failed to save order",Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateInStocks(){
        for(item in list){
            val stock = item.product.inStock - item.freeCount - item.count
            val soldQuantity = item.product.soldQuantity + item.count + item.freeCount
            databaseProductRef.child(item.product.id).child("soldQuantity").setValue(soldQuantity)
            databaseProductRef.child(item.product.id).child("inStock").setValue(stock)
        }
        Toast.makeText(requireContext(),"Stocks updated",Toast.LENGTH_SHORT).show()
    }

    private fun getTimeStamp(): String {
        val time = Calendar.getInstance()
        return binding.date.text.toString() + "/" + time.get(Calendar.HOUR_OF_DAY)
            .toString() + ":" + time.get(Calendar.MINUTE).toString()
    }

    private fun updateOrderDetails(paymentStatus: Boolean,amount:Float){
        databaseMasterRef.child(masterId).child("orderCount").get().addOnSuccessListener {
            var orderCount = it.value.toString().toInt()
            orderCount += 1
            databaseMasterRef.child(masterId).child("orderCount").setValue(orderCount)
        }
        if(!paymentStatus){
            databaseMasterRef.child(masterId).child("unpaidBill").get().addOnSuccessListener {
                var unpaidAmount = it.value.toString().toFloat()
                unpaidAmount += amount
                unpaidAmount = String.format("%.2f",unpaidAmount).toFloat()
                databaseMasterRef.child(masterId).child("unpaidBill").setValue(unpaidAmount)
            }
        }
    }

    override fun onDateSet(p0: DatePicker?, year: Int, month: Int, day: Int) {
        binding.date.setText("$day-${month+1}-$year")
    }

}
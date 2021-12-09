package com.example.ultimatepearl


import android.app.Dialog
import android.os.Bundle
import android.util.Log

import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import com.example.ultimatepearl.databinding.FragmentHomeBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import org.apache.poi.ss.formula.functions.Value
import java.lang.System.exit
import java.util.*
import kotlin.system.exitProcess

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    var flag1 = false
    var flag2 = false
    var flag3 = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_home,container,false)
        return binding.root
    }

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as DrawerAction).unlockDrawer()
        setNavigationAction()
        getData()
        //checkCustomerPayment()
    }*/

    override fun onStart() {
        super.onStart()
        (activity as DrawerAction).unlockDrawer()
        setNavigationAction()
        getData()
        //checkCustomerPayment()
    }

    private fun checkCustomerPayment() {
        val status = isCustomerPaid()
        Log.d("customer",status.toString())
        if(status) {
            showDialog()
        }
    }

    private fun showDialog() {
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.payment_due)
        dialog.window?.setBackgroundDrawable(R.drawable.round_border.toDrawable())
        dialog.setCancelable(false)
        val button = dialog.findViewById<Button>(R.id.closeApp)
        button.setOnClickListener {
            dialog.dismiss()
            exitProcess(0)
        }
        dialog.show()
    }

    private fun isCustomerPaid():Boolean{
        val time = Calendar.getInstance()
        val year = time.get(Calendar.YEAR)
        val month = time.get(Calendar.MONTH) + 1
        val day = time.get(Calendar.DAY_OF_MONTH)
        val dueYear = 2021
        val dueMonth = 11
        val dueDate = 15
       if(year<dueYear){
           return false
       }else{
           if(year==dueYear){
               return if(month<dueMonth){
                   false
               }else{
                   !(month==dueMonth && day<dueDate)
               }
           }else{
               return true
           }
       }
    }

    private fun setNavigationAction() {
        binding.nav.setOnClickListener {
            (activity as DrawerAction).openDrawer()
        }
    }

    override fun onPause() {
        super.onPause()
        (activity as DrawerAction).closeDrawer()
    }

    private fun getData(){
        FirebaseDatabase.getInstance().getReference(userId).child("MasterId").addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val mastersCount = (snapshot.value.toString().toInt() - 1).toString()
                binding.totalMasters.text = mastersCount
                flag1 = true
                updateUI()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })

        FirebaseDatabase.getInstance().getReference(userId).child("ProductId").addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val productCount = (snapshot.value.toString().toInt() - 1).toString()
                binding.totalProducts.text = productCount
                flag2 = true
                updateUI()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })

        FirebaseDatabase.getInstance().getReference(userId).child("OrderDetails").addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val totalOrders = snapshot.child("TotalOrders").value.toString()
                val totalAmount = String.format("%.2f",snapshot.child("TotalAmount").value.toString().toFloat())
                val unPaidBill = String.format("%.2f",snapshot.child("unPaidBill").value.toString().toFloat())
                binding.totalOrders.text = totalOrders
                binding.totalAmount.text = "$totalAmount ₹"
                binding.unPaidAmount.text = "$unPaidBill ₹"
                flag3 = true
                updateUI()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })
    }

    private fun updateUI(){
        if(flag1 && flag2 && flag3){
            binding.loading.visibility = View.GONE
            binding.subTitle.visibility = View.VISIBLE
            binding.main.visibility = View.VISIBLE
        }
    }

    private fun getTimeStamp(): String {
        val time = Calendar.getInstance()
        return time.get(Calendar.DAY_OF_MONTH).toString() + "-" + time.get(Calendar.MONTH)
            .toString() + "-" + time.get(Calendar.YEAR).toString() + "/" + time.get(Calendar.HOUR_OF_DAY)
            .toString() + ":" + time.get(Calendar.MINUTE).toString()
    }

}
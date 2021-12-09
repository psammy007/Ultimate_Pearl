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
import com.example.ultimatepearl.databinding.FragmentMastersOrdersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MastersOrdersFragment : Fragment() ,ClickEventHandler{

    private lateinit var binding:FragmentMastersOrdersBinding
    private lateinit var args: MastersOrdersFragmentArgs
    private lateinit var database: FirebaseDatabase
    private lateinit var orderList:MutableList<OrderTemp>
    private lateinit var adapter: RecyclerViewOrdersAdapter
    private lateinit var adapterData:MutableList<OrderTemp>
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_masters_orders,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        setUpMasterData()
        setUpBackAction()
        setUpCreateOrder()
        setEditAction()
        setSwitchActions()
        getOrders()
    }

    private fun setSwitchActions() {
        binding.unpaidSwitch.setOnCheckedChangeListener{_,_->
            if(binding.unpaidSwitch.isChecked){
                showUnPaidOrders()
            }else{
                showAllOrders()
            }
        }
    }

    private fun showUnPaidOrders(){
        adapterData.clear()
        for(order in orderList){
            if(!order.paymentStatus){
                adapterData.add(0,order)
            }
        }
        adapterData.sortBy { it.timeStamp }
        adapter.notifyDataSetChanged()
        updateUI("No unpaid orders")
    }

    private fun showAllOrders(){
        adapterData.clear()
        adapterData.addAll(orderList)
        adapterData.sortBy { it.timeStamp }
        adapter.notifyDataSetChanged()
        updateUI("No Orders\n" + "Tap + to create orders")
    }

    private fun initializeVariables() {
        args = MastersOrdersFragmentArgs.fromBundle(requireArguments())
        database = FirebaseDatabase.getInstance()
        orderList = ArrayList()
        adapterData = ArrayList()
        adapter = RecyclerViewOrdersAdapter(adapterData,this)
        val ordersList = binding.ordersList
        ordersList.layoutManager = GridLayoutManager(activity,1)
        ordersList.adapter = adapter
        (activity as DrawerAction).lockDrawer()
    }

    private fun setUpMasterData(){
        database.getReference(userId).child("Masters").orderByChild("id").equalTo(args.masterId).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child(args.masterId).child("name").value.toString()
                val designation = snapshot.child(args.masterId).child("addressLine").value.toString()
                var unpaidAmount = snapshot.child(args.masterId).child("unpaidBill").value.toString().toFloat()
                unpaidAmount = String.format("%.2f",unpaidAmount).toFloat()
                var textView = binding.masterName
                textView.text = name
                textView = binding.designation
                textView.text = designation
                textView = binding.unpaidAmount
                textView.text = "$unpaidAmount  \u20B9"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })
    }

    private fun getOrders(){
        database.getReference(userId).child("Orders").child(args.masterId).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    val key = postSnapshot.key.toString()
                    val bill = postSnapshot.child("bill").value.toString().toFloat()
                    val timeStamp = postSnapshot.child("timeStamp").value.toString()
                    val paymentStatus = postSnapshot.child("paymentStatus").value.toString().toBoolean()
                    val orderTemp = OrderTemp(key,bill,paymentStatus,timeStamp)
                    this@MastersOrdersFragment.orderList.add(0,orderTemp)
                }
                adapterData.addAll(orderList)
                this@MastersOrdersFragment.updateData()

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })
    }

    private fun updateData(){
        adapter.notifyDataSetChanged()
        updateUI("No Orders\nTap + to create orders")
    }

    private fun updateUI(msg:String) {
        binding.loading.visibility = View.GONE
        if(adapterData.isEmpty()){
            binding.ordersList.visibility = View.GONE
            binding.message.text = msg
            binding.message.visibility = View.VISIBLE
        }else{
            binding.ordersList.visibility = View.VISIBLE
            binding.message.visibility = View.GONE
        }
    }

    private fun setUpCreateOrder(){
        binding.createOrder.setOnClickListener {
            view?.findNavController()?.navigate(MastersOrdersFragmentDirections.actionMastersOrdersFragmentToOrderItemsFragment(args.masterId))
        }
    }

    private fun setUpBackAction() {
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

    override fun openInDetail(Id: String) {
        view?.findNavController()?.navigate(MastersOrdersFragmentDirections.actionMastersOrdersFragmentToOrderSummaryFragment(args.masterId,Id))
    }

    private fun setEditAction(){
        binding.edit.setOnClickListener {
            view?.findNavController()?.navigate(MastersOrdersFragmentDirections.actionMastersOrdersFragmentToCreateMasterFragment(args.masterId))
        }
    }

}
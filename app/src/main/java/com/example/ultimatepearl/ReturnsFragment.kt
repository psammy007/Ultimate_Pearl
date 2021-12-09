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
import com.example.ultimatepearl.databinding.FragmentReturnsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ReturnsFragment(val masterId:String) : Fragment() ,ClickEventHandler{

    private lateinit var binding:FragmentReturnsBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var returnList:MutableList<Sample>
    private lateinit var adapter: RecyclerViewReturnAdapter
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_returns,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        setUpAddReturn()
        getReturns()
    }

    private fun initializeVariables() {
        database = FirebaseDatabase.getInstance()
        returnList = ArrayList()
        adapter = RecyclerViewReturnAdapter(returnList,this)
        val returnList = binding.returnsList
        returnList.layoutManager = GridLayoutManager(activity,1)
        returnList.adapter = adapter
        (activity as DrawerAction).lockDrawer()
    }

    private fun updateUI(msg:String) {
        binding.loading.visibility = View.GONE
        if(returnList.isEmpty()){
            binding.returnsList.visibility = View.GONE
            binding.message.text = msg
            binding.message.visibility = View.VISIBLE
        }else{
            binding.returnsList.visibility = View.VISIBLE
            binding.message.visibility = View.GONE
        }
    }


    private fun getReturns(){
        database.getReference(userId).child("Returns").child(masterId).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (postSnapshot in snapshot.children){
                    val key = postSnapshot.key.toString()
                    val salesman = postSnapshot.child("salesman").child("name").value.toString()
                    val timeStamp = postSnapshot.child("timeStamp").value.toString()
                    val sample = Sample(key,timeStamp,salesman)
                    this@ReturnsFragment.returnList.add(0,sample)
                }
                this@ReturnsFragment.updateUI("No orders.\nTap + to add return")

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })
    }

    private fun setUpAddReturn(){
        binding.addReturns.setOnClickListener {
            view?.findNavController()?.navigate(MasterDetailFragmentDirections.actionMasterDetailFragmentToAddReturnsFragment(masterId))
        }
    }

    override fun openInDetail(Id: String) {
        view?.findNavController()?.navigate(MasterDetailFragmentDirections.actionMasterDetailFragmentToReturnsSummaryFragment(masterId,Id))
    }

}
package com.example.ultimatepearl

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ultimatepearl.databinding.FragmentCreateSalesmanBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase

class CreateSalesmanFragment : Fragment() {

    private lateinit var binding:FragmentCreateSalesmanBinding
    private lateinit var args: CreateSalesmanFragmentArgs
    private lateinit var databaseRef: DatabaseReference
    val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    lateinit var refId:String
    var salesManId = ""

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_create_salesman,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        setBackActions()
        setSaveAction()
    }

    private fun initializeVariables() {
        args = CreateSalesmanFragmentArgs.fromBundle(requireArguments())
        databaseRef = FirebaseDatabase.getInstance().getReference(userId)
        if(args.salesmanId.toString().isEmpty()){
            fetchId()
        }else{
            fetchDetails()
        }
    }

    private fun fetchId() {
        databaseRef.child("SalesmanId").get().addOnSuccessListener {
            refId = it.value.toString()
            salesManId = "SR" + "0".repeat(4-refId.length) + refId
            binding.id.setText(salesManId)
        }
    }

    private fun fetchDetails(){
        salesManId = args.salesmanId
        databaseRef.child("Salesmans").child(salesManId).get().addOnSuccessListener {
            val name = it.child("name").value.toString()
            val contact = it.child("contact").value.toString()
            binding.name.setText(name)
            binding.phoneNo.setText(contact)
            binding.id.setText(salesManId)
        }
    }

    private fun setBackActions() {
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

    private fun setSaveAction(){
        binding.createSalesman.setOnClickListener {
            val name = binding.name.text.toString()
            val contact = binding.phoneNo.text.toString()
            if(name.isEmpty()){
                binding.name.error = "Enter name"
                binding.name.requestFocus()
            }else if(contact.isEmpty()){
                binding.phoneNo.error = "Enter contact no"
                binding.phoneNo.requestFocus()
            }else if(contact.length != 10){
                binding.phoneNo.error = "Invalid contact no"
                binding.phoneNo.requestFocus()
            }else{
                binding.loading.visibility = View.VISIBLE
                saveSalesman(salesManId,name,contact)
            }
        }
    }

    private fun saveSalesman(salesManId: String, name: String, contact: String) {
        val salesman = Salesman(salesManId,name,contact)
        databaseRef.child("Salesmans").child(salesManId).setValue(salesman).addOnSuccessListener {
            if(args.salesmanId.isEmpty()) {
                databaseRef.child("SalesmanId").setValue(refId.toInt() + 1).addOnSuccessListener {
                    view?.findNavController()?.navigateUp()
                }
            }else{
                view?.findNavController()?.navigateUp()
            }
        }
    }

}
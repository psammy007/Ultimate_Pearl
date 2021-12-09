package com.example.ultimatepearl

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ultimatepearl.databinding.FragmentProductDetailBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProductDetailFragment : Fragment() {

    private lateinit var binding:FragmentProductDetailBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var args: ProductDetailFragmentArgs
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_product_detail,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        getDetails()
        setBackAction()
        setEditAction()
    }

    private fun initializeVariables() {
        args = ProductDetailFragmentArgs.fromBundle(requireArguments())
        databaseRef = FirebaseDatabase.getInstance().getReference(userId).child("Products")
        (activity as DrawerAction).lockDrawer()
    }

    private fun getDetails(){
        databaseRef.orderByChild("id").equalTo(args.productId).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child(args.productId).child("name").value.toString()
                val id = snapshot.child(args.productId).child("id").value.toString()
                val taxRate = snapshot.child(args.productId).child("taxRate").value.toString()
                val mrp = snapshot.child(args.productId).child("mrp").value.toString()
                val stock = snapshot.child(args.productId).child("inStock").value.toString()
                var textView = binding.productName
                textView.text = name
                textView = binding.code
                textView.text = id
                textView = binding.mrp
                textView.text = mrp
                textView = binding.taxRate
                textView.text = taxRate
                textView = binding.stock
                textView.text = stock

            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })
    }

    private fun setBackAction(){
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

    private fun setEditAction(){
        binding.edit.setOnClickListener {
            view?.findNavController()?.navigate(ProductDetailFragmentDirections.actionProductDetailFragmentToCreateProductFragment(args.productId))
        }
    }

}
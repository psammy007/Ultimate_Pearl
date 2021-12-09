package com.example.ultimatepearl

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ultimatepearl.databinding.FragmentCreateProductBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CreateProductFragment : Fragment() {

    private lateinit var binding:FragmentCreateProductBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var args: CreateProductFragmentArgs
    var productRef = ""
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var productGroupList: MutableList<ProductGroup>
    private lateinit var selectedProductGroup:ProductGroup
    private lateinit var productGroupRef :DatabaseReference
    private lateinit var productGroupAdapter: ArrayAdapter<ProductGroup>
    var soldQuantity = 0L

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_create_product,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        setUpSaveAction()
        setBackAction()
        fetchProductGroupList()
        if(args.productId.isNotEmpty())
            fetchDetails()
        else
            getProductId()
    }

    private fun initializeVariables() {
        productGroupRef = FirebaseDatabase.getInstance().getReference(userId).child("ProductGroups")
        databaseRef = FirebaseDatabase.getInstance().getReference(userId).child("Products")
        args = CreateProductFragmentArgs.fromBundle(requireArguments())
        (activity as DrawerAction).lockDrawer()
        productGroupList = ArrayList()
        productGroupList.add(0, ProductGroup("0","--Select product group--"))
        productGroupList.add(1, ProductGroup("1","--Add product group--"))
        setProductList()
    }

    private fun fetchProductGroupList(){
        productGroupRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                productGroupList.clear()
                productGroupList.add(0, ProductGroup("0","--Select product group--"))
                productGroupList.add(1, ProductGroup("1","--Add product group--"))
                for(postSnapshot in snapshot.children){
                    val id = postSnapshot.key.toString()
                    val name = postSnapshot.child("group").value.toString()
                    val productGroup = ProductGroup(id,name)
                    productGroupList.add(2,productGroup)
                }
                productGroupAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun setProductList(){
        productGroupAdapter = ArrayAdapter(requireContext(),R.layout.support_simple_spinner_dropdown_item,productGroupList)
        val spinner = binding.group
        spinner.adapter = productGroupAdapter

        spinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if(position==1)
                    showAddProductGroupPopup()
                else
                    selectedProductGroup = productGroupList[position]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }

    }

    private fun showAddProductGroupPopup(){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.add_area)
        dialog.window?.setBackgroundDrawable(R.drawable.round_border.toDrawable())
        dialog.setCancelable(true)
        val textView = dialog.findViewById<TextView>(R.id.msg)
        textView.text = "Enter product group"
        val cancel = dialog.findViewById<Button>(R.id.cancel)
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        val addArea = dialog.findViewById<Button>(R.id.addArea)
        addArea.setOnClickListener {
            val area = dialog.findViewById<EditText>(R.id.area).text.toString()
            addProductGroupToDatabase(area)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun addProductGroupToDatabase(group:String){
        val id = productGroupRef.push().key.toString()
        val productGroup = ProductGroup(id,group)
        productGroupRef.child(id).setValue(productGroup).addOnSuccessListener {
            Toast.makeText(requireContext(),"Product group added",Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpSaveAction(){
        binding.createProduct.setOnClickListener {
            val id = binding.id.text.toString()
            val name = binding.name.text.toString()
            val ptrRate = binding.ptrRate.text.toString()
            val taxRate = binding.taxRate.text.toString()
            val price = binding.price.text.toString()
            val hsnCode = binding.hsnCode.text.toString()
            val stock = binding.stock.text.toString()
            val base = binding.base.text.toString()
            val free = binding.free.text.toString()
            val discount = binding.discount.text.toString()
            when {
                id.isEmpty() -> {
                    binding.id.error = "Enter code"
                    binding.id.requestFocus()
                }
                name.isEmpty() -> {
                    binding.name.error = "Enter name"
                    binding.name.requestFocus()
                }
                selectedProductGroup.group=="--Select product group--"-> {
                    Toast.makeText(requireContext(),"Select product group",Toast.LENGTH_SHORT).show()
                }
                ptrRate.isEmpty() -> {
                    binding.ptrRate.error = "Enter ptr rate"
                    binding.ptrRate.requestFocus()
                }
                taxRate.isEmpty() -> {
                    binding.taxRate.error = "Enter tax rate"
                    binding.taxRate.requestFocus()
                }
                price.isEmpty() -> {
                    binding.price.error = "Enter price"
                    binding.price.requestFocus()
                }
                /*hsnCode.isEmpty() -> {
                    binding.hsnCode.error = "Enter hsn code"
                    binding.hsnCode.requestFocus()
                }*/
                stock.isEmpty() -> {
                    binding.stock.error = "Enter stock"
                    binding.stock.requestFocus()
                }
                base.isEmpty() -> {
                    binding.base.error = "Enter base quantity"
                    binding.base.requestFocus()
                }
                free.isEmpty() -> {
                    binding.free.error = "Enter free quantity"
                    binding.free.requestFocus()
                }
                discount.isEmpty() -> {
                    binding.discount.error = "Enter discount rate"
                    binding.discount.requestFocus()
                }
                discount.toInt() > 100 ->{
                    binding.discount.error = "discount rate should be less than or equal to 100"
                    binding.discount.requestFocus()
                }
                else -> {
                    binding.createProduct.isClickable = false
                    binding.loading.visibility = View.VISIBLE
                    saveProduct(id, name, selectedProductGroup, ptrRate.toFloat(), price.toFloat(),hsnCode, taxRate.toFloat(), stock.toInt(), Scheme(base.toInt(), free.toInt()), discount.toFloat())
                }
            }
        }
    }

    private fun fetchDetails(){
        databaseRef.child(args.productId).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child("name").value.toString()
                val id = snapshot.child("id").value.toString()
                val groupName = snapshot.child("productGroup").child("group").value.toString()
                val ptrRate = snapshot.child("ptrRate").value.toString()
                val mrp = snapshot.child("mrp").value.toString()
                val hsnCode = snapshot.child("hsnCode").value.toString()
                val taxRate = snapshot.child("taxRate").value.toString()
                val stock = snapshot.child("inStock").value.toString()
                soldQuantity = snapshot.child("soldQuantity").value.toString().toLong()
                val base = snapshot.child("scheme").child("base").value.toString()
                val free = snapshot.child("scheme").child("free").value.toString()
                val discount = snapshot.child("discount").value.toString()

                for(i in productGroupList.indices){
                    if(productGroupList[i].group==groupName)
                        binding.group.setSelection(i)
                }

                var editText = binding.name
                editText.setText(name)
                editText = binding.id
                editText.setText(id)
                editText = binding.price
                editText.setText(mrp)
                editText = binding.ptrRate
                editText.setText(ptrRate)
                editText = binding.taxRate
                editText.setText(taxRate)
                editText = binding.hsnCode
                editText.setText(hsnCode)
                editText = binding.stock
                editText.setText(stock)
                editText = binding.base
                editText.setText(base)
                editText = binding.free
                editText.setText(free)
                editText = binding.discount
                editText.setText(discount)
                val textView = binding.title
                textView.text = "Edit Product"
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })
    }

    private fun getProductId(){
        FirebaseDatabase.getInstance().getReference(userId).child("ProductId").addListenerForSingleValueEvent(
            object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot){
                    productRef = snapshot.value.toString()
                    val id = "0".repeat(5-productRef.length) + productRef
                    binding.id.setText(id)
                }

                override fun onCancelled(error: DatabaseError){
                    Log.d("Database error",error.message)
                }
            }
        )
    }

    private fun saveProduct(id:String,name:String,group:ProductGroup,ptrRate:Float,mrp:Float,hsnCode:String,taxRate:Float,stock:Int,scheme:Scheme,discount:Float) {
        val product = Product(id,name, group, ptrRate, mrp,taxRate,hsnCode,stock,scheme,discount,soldQuantity)
        databaseRef.child(id).setValue(product).addOnSuccessListener {
            if(args.productId.isEmpty())
                FirebaseDatabase.getInstance().getReference(userId).child("ProductId").setValue(productRef.toInt() + 1)
            Toast.makeText(requireContext(),"Product saved successfully",Toast.LENGTH_SHORT).show()
            view?.findNavController()?.navigateUp()
        }.addOnFailureListener {
            Toast.makeText(requireContext(),"Failed to save product",Toast.LENGTH_SHORT).show()
            binding.createProduct.isClickable = true
            binding.loading.visibility = View.GONE
        }
    }

    private fun setBackAction() {
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

}
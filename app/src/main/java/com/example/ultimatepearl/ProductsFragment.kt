package com.example.ultimatepearl

import android.app.Dialog
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ultimatepearl.databinding.FragmentProductsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProductsFragment : Fragment(),ClickEventHandler {

    private lateinit var binding:FragmentProductsBinding
    private lateinit var databaseRef: DatabaseReference
    private lateinit var productsList: MutableList<Product>
    private lateinit var adapterData:MutableList<Product>
    private lateinit var adapter: RecyclerViewProductsAdapter
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var generatePattern:String
    private lateinit var dialog: Dialog
    private lateinit var productGroupList: MutableList<ProductGroup>
    private lateinit var productGroupAdapter:ArrayAdapter<ProductGroup>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_products,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        setBacAction()
        setAddProductAction()
        getProducts()
        getProductGroups()
        setFilterButton()
        implementSearch()
    }

    private fun getProductGroups(){
        FirebaseDatabase.getInstance().getReference(userId).child("ProductGroups").addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    val id = postSnapshot.key.toString()
                    val name = postSnapshot.child("group").value.toString()
                    val productGroup = ProductGroup(id,name)
                    productGroupList.add(1,productGroup)
                }
                setFilter()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }


    private fun initializeVariables() {
        productsList = ArrayList()
        adapterData = ArrayList()
        productGroupList = ArrayList()
        productGroupList.add(0,ProductGroup("0","--Clear filter--"))
        productGroupAdapter = ArrayAdapter(requireContext(),R.layout.support_simple_spinner_dropdown_item,productGroupList)
        databaseRef = FirebaseDatabase.getInstance().getReference(userId).child("Products")
        val productList = binding.productsList
        adapter = RecyclerViewProductsAdapter(adapterData,this)
        productList.layoutManager = GridLayoutManager(activity,1)
        productList.adapter = adapter
        (activity as DrawerAction).lockDrawer()
    }

    private fun setFilter() {
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.filter)
        dialog.window?.setBackgroundDrawable(R.drawable.round_border.toDrawable())
        dialog.setCancelable(true)
        val textView = dialog.findViewById<TextView>(R.id.msg)
        textView.text = "Select product id"
        val cancel = dialog.findViewById<Button>(R.id.cancel)
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        val spinner = dialog.findViewById<Spinner>(R.id.spinner)
        spinner.adapter = productGroupAdapter
        val search = dialog.findViewById<Button>(R.id.search)
        search.setOnClickListener {
            val areaSpinner = dialog.findViewById<Spinner>(R.id.spinner)
            val selectedAreaIndex = areaSpinner.selectedItemPosition
            if(selectedAreaIndex==0)
                clearFilter()
            else
                filterMasterByAreas(productGroupList[selectedAreaIndex])
            dialog.dismiss()
        }

    }

    private fun clearFilter(){
        adapterData.clear()
        adapterData.addAll(productsList)
        adapter.notifyDataSetChanged()
        updateUI("No products\n" + "Tap + to create Master")
    }

    private fun filterMasterByAreas(productGroup: ProductGroup) {
        adapterData.clear()
       for(product in productsList){
           if(product.productGroup.group==productGroup.group)
               adapterData.add(0,product)
       }
        adapter.notifyDataSetChanged()
        updateUI("No products from ${productGroup.group}")
    }

    private fun getProducts() {
        databaseRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                this@ProductsFragment.productsList.clear()
                for (postSnapshot in snapshot.children){
                    val id = postSnapshot.key.toString()
                    val name = postSnapshot.child("name").value.toString()
                    val groupId = postSnapshot.child("productGroup").child("id").value.toString()
                    val groupName = postSnapshot.child("productGroup").child("group").value.toString()
                    val hsnCode = postSnapshot.child("hsnCode").value.toString()
                    val ptrRate = postSnapshot.child("ptrRate").value.toString().toFloat()
                    val taxRate = postSnapshot.child("taxRate").value.toString().toFloat()
                    val mrp = postSnapshot.child("mrp").value.toString().toFloat()
                    val stock = postSnapshot.child("inStock").value.toString()
                    val base = postSnapshot.child("scheme").child("base").value.toString()
                    val free = postSnapshot.child("scheme").child("free").value.toString()
                    val discount = postSnapshot.child("discount").value.toString()
                    val soldQuantity = postSnapshot.child("soldQuantity").value.toString().toLong()
                    val product = Product(
                        id,
                        name,
                        ProductGroup(groupId,groupName),
                        ptrRate,
                        mrp,
                        taxRate,
                        hsnCode,
                        stock.toInt(),
                        Scheme(base.toInt(),free.toInt()),
                        discount.toFloat(),soldQuantity)
                    this@ProductsFragment.productsList.add(0,product)
                }
                this@ProductsFragment.productsList.sortBy { it.name }
                this@ProductsFragment.updateData()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })
    }

    private fun setFilterButton(){
        binding.filter.setOnClickListener {
            dialog.show()
        }
    }

    private fun updateData(){
        adapterData.clear()
        adapterData.addAll(productsList)
        adapter.notifyDataSetChanged()
        updateUI("No Products.\nTap + to add product")
    }

    private fun updateUI(msg:String){
        binding.loading.visibility = View.GONE
        if(adapterData.isEmpty()){
            binding.productsList.visibility = View.GONE
            binding.message.text = msg
            binding.message.visibility = View.VISIBLE
        }else{
            binding.filter.visibility = View.VISIBLE
            binding.searchBar.visibility = View.VISIBLE
            binding.message.visibility = View.GONE
            binding.productsList.visibility = View.VISIBLE
        }
    }

    private fun setBacAction(){
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

    private fun setAddProductAction(){
        binding.addProduct.setOnClickListener {
            view?.findNavController()?.navigate(ProductsFragmentDirections.actionProductsFragmentToCreateProductFragment(""))
        }
    }

    override fun openInDetail(Id: String) {
        view?.findNavController()?.navigate(ProductsFragmentDirections.actionProductsFragmentToProductDetailFragment(Id))
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
                    val searchResult = ArrayList<Product>()
                    for(product in productsList){
                        if(pattern.containsMatchIn(product.name)){
                            searchResult.add(0,product)
                        }
                    }
                    adapterData.clear()
                    adapterData.addAll(searchResult.sortedBy { it.name })
                    adapter.notifyDataSetChanged()
                    if(searchResult.size==0){
                        updateUI("No Match")
                    }else{
                        updateUI("")
                        adapter.notifyDataSetChanged()
                    }
                }else{
                    adapterData.clear()
                    adapterData.addAll(productsList)
                    adapter.notifyDataSetChanged()
                    updateUI("No products\nTap + to create product")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

}
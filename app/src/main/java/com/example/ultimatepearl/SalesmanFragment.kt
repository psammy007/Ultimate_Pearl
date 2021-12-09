package com.example.ultimatepearl

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ultimatepearl.databinding.FragmentSalesmanBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class SalesmanFragment : Fragment(),ClickEventHandler {

    private lateinit var binding:FragmentSalesmanBinding
    private lateinit var database:DatabaseReference
    val userId = FirebaseAuth.getInstance().currentUser?.uid.toString()
    private lateinit var salesmanList: MutableList<Salesman>
    private lateinit var adapterData: MutableList<Salesman>
    private lateinit var salesmanRecyclerViewAdapter: SalesmanRecyclerViewAdapter
    private lateinit var generatePattern:String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_salesman,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        getSalesman()
        setSalesmanAdapter()
        setBackAction()
        setAddAction()
        implementSearch()
    }

    private fun initializeVariables() {
        database = FirebaseDatabase.getInstance().getReference(userId)
        salesmanList = ArrayList()
        adapterData = ArrayList()
        salesmanRecyclerViewAdapter = SalesmanRecyclerViewAdapter(adapterData,this)
    }

    private fun getSalesman(){
        database.child("Salesmans").addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    val name = postSnapshot.child("name").value.toString()
                    val contact = postSnapshot.child("contact").value.toString()
                    val id = postSnapshot.key.toString()
                    val salesman = Salesman(id,name, contact)
                    salesmanList.add(0,salesman)
                    /*database.child("Orders").orderByChild("salesman/name").equalTo(name).get().addOnSuccessListener {
                        Log.d("salesman",it.toString())
                    }*/
                }
                adapterData.clear()
                adapterData.addAll(salesmanList)
                updateUI("No salesman.\nTap + to add salesman")
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    private fun updateUI(msg:String){
        salesmanRecyclerViewAdapter.notifyDataSetChanged()
        binding.loading.visibility = View.GONE
        if(adapterData.isEmpty()){
            binding.message.text = msg
            binding.message.visibility = View.VISIBLE
            binding.salesmanList.visibility = View.GONE
        }else{
            binding.searchBar.visibility = View.VISIBLE
            binding.message.visibility = View.GONE
            binding.salesmanList.visibility = View.VISIBLE
        }
    }

    private fun setSalesmanAdapter(){
        val salesmanRecyclerView = binding.salesmanList
        salesmanRecyclerView.layoutManager = GridLayoutManager(activity,1)
        salesmanRecyclerView.adapter = salesmanRecyclerViewAdapter
    }

    private fun setBackAction() {
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

    private fun setAddAction(){
        binding.addSalesman.setOnClickListener {
            view?.findNavController()?.navigate(SalesmanFragmentDirections.actionSalesmanFragmentToCreateSalesmanFragment(""))
        }
    }

    override fun openInDetail(Id: String) {
        view?.findNavController()?.navigate(SalesmanFragmentDirections.actionSalesmanFragmentToCreateSalesmanFragment(Id))
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
                    val searchResult = ArrayList<Salesman>()
                    for(salesman in salesmanList){
                        if(pattern.containsMatchIn(salesman.name)){
                            searchResult.add(0,salesman)
                        }
                    }
                    adapterData.clear()
                    adapterData.addAll(searchResult.sortedBy { it.name })
                    salesmanRecyclerViewAdapter.notifyDataSetChanged()
                    if(searchResult.size==0){
                        updateUI("No Match")
                    }else{
                        updateUI("")
                        salesmanRecyclerViewAdapter.notifyDataSetChanged()
                    }
                }else{
                    adapterData.clear()
                    adapterData.addAll(salesmanList)
                    salesmanRecyclerViewAdapter.notifyDataSetChanged()
                    updateUI("No products\nTap + to create product")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

}
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
import android.widget.EditText
import android.widget.Spinner
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.example.ultimatepearl.databinding.FragmentMastersBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class MastersFragment : Fragment(),ClickEventHandler {

    private lateinit var binding:FragmentMastersBinding
    private lateinit var masterList: MutableList<MasterTemp>
    private lateinit var adapterData:MutableList<MasterTemp>
    private lateinit var adapter:RecyclerViewMastersAdapter
    private lateinit var databaseRef: DatabaseReference
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var generatePattern:String
    private lateinit var areaList: MutableList<Area>
    private lateinit var dialog: Dialog
    private lateinit var areaAdapter:ArrayAdapter<Area>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_masters,container,false)
        return binding.root
    }

    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        getMasters()
        getAreas()
        setFilterButton()
        setAddAction()
        setBackAction()
        implementSearch()
    }*/

    override fun onStart() {
        super.onStart()
        initializeVariables()
        getMasters()
        getAreas()
        setFilterButton()
        setAddAction()
        setBackAction()
        implementSearch()
    }

    private fun setFilter() {
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.filter)
        dialog.window?.setBackgroundDrawable(R.drawable.round_border.toDrawable())
        dialog.setCancelable(true)
        val cancel = dialog.findViewById<Button>(R.id.cancel)
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        val spinner = dialog.findViewById<Spinner>(R.id.spinner)
        spinner.adapter = areaAdapter
        val search = dialog.findViewById<Button>(R.id.search)
        search.setOnClickListener {
            val areaSpinner = dialog.findViewById<Spinner>(R.id.spinner)
            val selectedAreaIndex = areaSpinner.selectedItemPosition
            if(selectedAreaIndex==0)
                clearFilter()
            else
                filterMasterByAreas(areaList[selectedAreaIndex])
            dialog.dismiss()
        }

    }

    private fun clearFilter(){
        adapterData.clear()
        adapterData.addAll(masterList)
        adapter.notifyDataSetChanged()
        updateUI("No masters\n" + "Tap + to create Master")
    }

    private fun filterMasterByAreas(area: Area) {
        adapterData.clear()
        for(masters in masterList){
            if(masters.area.area==area.area)
                adapterData.add(0,masters)
        }
        adapter.notifyDataSetChanged()
        updateUI("No customers from ${area.area}")
    }

    private fun getAreas(){
        FirebaseDatabase.getInstance().getReference(userId).child("Areas").addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for(postSnapshot in snapshot.children){
                    val id = postSnapshot.key.toString()
                    val name = postSnapshot.child("area").value.toString()
                    val area = Area(id,name)
                    areaList.add(1,area)
                }
               setFilter()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun setFilterButton(){
        binding.filter.setOnClickListener {
            dialog.show()
        }
    }

    private fun initializeVariables() {
        masterList = ArrayList()
        adapterData = ArrayList()
        areaList = ArrayList()
        areaAdapter = ArrayAdapter(requireContext(),R.layout.support_simple_spinner_dropdown_item,areaList)
        areaList.add(0,Area("0","--Clear filter--"))
        adapter = RecyclerViewMastersAdapter(adapterData,this)
        val mastersList = binding.mastersList
        mastersList.layoutManager = GridLayoutManager(activity,1)
        mastersList.adapter = adapter
        databaseRef = FirebaseDatabase.getInstance().getReference(userId).child("Masters")
        //(activity as DrawerAction).lockDrawer()
    }

    private fun getMasters(){
        databaseRef.addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                this@MastersFragment.masterList.clear()
                for (postSnapshot in snapshot.children){
                    val key = postSnapshot.key.toString()
                    val name = postSnapshot.child("name").value.toString()
                    val orderCount = postSnapshot.child("orderCount").value.toString().toInt()
                    val areaId = postSnapshot.child("area").child("id").value.toString()
                    val areaName = postSnapshot.child("area").child("area").value.toString()
                    val master = MasterTemp(key,name,orderCount,Area(areaId,areaName))
                    this@MastersFragment.masterList.add(0,master)
                }
                this@MastersFragment.updateData()
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })

    }

    private fun updateData(){
        masterList.sortBy { it.name }
        adapterData.clear()
        adapterData.addAll(masterList)
        adapter.notifyDataSetChanged()
        updateUI("No masters\nTap + to create Master")
    }

    private fun updateUI(msg:String) {
        binding.loading.visibility = View.GONE
        if(adapterData.isEmpty()){
            binding.mastersList.visibility = View.GONE
            binding.message.text = msg
            binding.message.visibility = View.VISIBLE
        }else{
            binding.searchBar.visibility = View.VISIBLE
            binding.filter.visibility = View.VISIBLE
            binding.mastersList.visibility = View.VISIBLE
            binding.message.visibility = View.GONE
        }
    }

    private fun setAddAction(){
        binding.addMaster.setOnClickListener {
            view?.findNavController()?.navigate(MastersFragmentDirections.actionMastersFragmentToCreateMasterFragment(""))
        }
    }

    private fun setBackAction(){
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

    override fun openInDetail(Id: String) {
        view?.findNavController()?.navigate(MastersFragmentDirections.actionMastersFragmentToMasterDetailFragment(Id))
    }

    private fun implementSearch(){
        binding.searchBar.addTextChangedListener(object :TextWatcher{
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
                    val searchResult = ArrayList<MasterTemp>()
                    for(master in masterList){
                        if(pattern.containsMatchIn(master.name)){
                            searchResult.add(0,master)
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
                    adapterData.addAll(masterList)
                    adapter.notifyDataSetChanged()
                    updateUI("No masters\nTap + to create Master")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }

        })
    }

}
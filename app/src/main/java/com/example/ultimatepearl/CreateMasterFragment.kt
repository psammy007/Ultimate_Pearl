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
import com.example.ultimatepearl.databinding.FragmentCreateMasterBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class CreateMasterFragment : Fragment() {

    lateinit var binding:FragmentCreateMasterBinding
    private lateinit var databaseRef: DatabaseReference
    var refId = ""
    var orderCount = 0
    var returnCount = 0
    private lateinit var args: CreateMasterFragmentArgs
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var areaList: MutableList<Area>
    private lateinit var selectedArea:Area
    private lateinit var areaRef :DatabaseReference
    private lateinit var areaAdapter:ArrayAdapter<Area>
   var prevUnpaidBill:Float = 0F

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_create_master,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        fetchAreas()
        if(args.masterId.isEmpty())
            getMasterId()
        else
            fetchDetails()
        setUpSaveAction()
        setUpBackAction()
    }

    private fun initializeVariables() {
        areaList = ArrayList()
        areaList.add(0,Area("0","--Select Area--"))
        areaList.add(1,Area("1","--Add Area--"))
        databaseRef = FirebaseDatabase.getInstance().getReference(userId).child("Masters")
        areaRef = FirebaseDatabase.getInstance().getReference(userId).child("Areas")
        (activity as DrawerAction).lockDrawer()
        args = CreateMasterFragmentArgs.fromBundle(requireArguments())
        setAreaList()
    }

    private fun fetchAreas(){
        areaRef.addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                areaList.clear()
                areaList.add(0,Area("0","--Select Area--"))
                areaList.add(1,Area("1","--Add Area--"))
                for(postSnapshot in snapshot.children){
                    val id = postSnapshot.key.toString()
                    val name = postSnapshot.child("area").value.toString()
                    val area = Area(id,name)
                    areaList.add(2,area)
                }
                areaAdapter.notifyDataSetChanged()
            }

            override fun onCancelled(error: DatabaseError) {

            }

        })
    }

    private fun setAreaList(){
        areaAdapter = ArrayAdapter(requireContext(),R.layout.support_simple_spinner_dropdown_item,areaList)
        val areaSpinner = binding.area
        areaSpinner.adapter = areaAdapter

        areaSpinner.onItemSelectedListener = object:AdapterView.OnItemSelectedListener{
            override fun onItemSelected(p0: AdapterView<*>?, p1: View?, position: Int, p3: Long) {
                if(position==1)
                    showAddAreaPopup()
                else
                    selectedArea = areaList[position]
            }

            override fun onNothingSelected(p0: AdapterView<*>?) {

            }

        }
    }

    private fun showAddAreaPopup(){
        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.add_area)
        dialog.window?.setBackgroundDrawable(R.drawable.round_border.toDrawable())
        dialog.setCancelable(true)
        val cancel = dialog.findViewById<Button>(R.id.cancel)
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        val addArea = dialog.findViewById<Button>(R.id.addArea)
        addArea.setOnClickListener {
            val area = dialog.findViewById<EditText>(R.id.area).text.toString()
            addAreaToDatabase(area)
            dialog.dismiss()
        }
        dialog.show()
    }

    private fun addAreaToDatabase(areaName:String){
        val id = areaRef.push().key.toString()
        val area = Area(id,areaName)
        areaRef.child(id).setValue(area).addOnSuccessListener {
            Toast.makeText(requireContext(),"Area added",Toast.LENGTH_SHORT).show()
        }
    }



    private fun getMasterId(){
        FirebaseDatabase.getInstance().getReference(userId).child("MasterId").addListenerForSingleValueEvent(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                refId = snapshot.value.toString()
                val masterId = "C" +  "0".repeat(5-(refId.length)) + refId
                binding.id.setText(masterId)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })
    }

    private fun fetchDetails(){
        binding.id.setText(args.masterId)
        FirebaseDatabase.getInstance().getReference(userId).child("Masters").child(args.masterId).addListenerForSingleValueEvent(
            object:ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child("name").value.toString()
                    val addressLine = snapshot.child("addressLine").value.toString()
                    val city = snapshot.child("city").value.toString()
                    val email = snapshot.child("email").value.toString()
                    val contact = snapshot.child("contact").value.toString()
                    val gstType = snapshot.child("gst_Type").value.toString()
                    val areaId = snapshot.child("area").child("id").value.toString()
                    val areaName = snapshot.child("area").child("area").value.toString()
                    val gstNumber = snapshot.child("gst_Number").value.toString()
                    val drugLicNo = snapshot.child("drugLicNo").value.toString()
                    val fssaiNo = snapshot.child("fssaiNo").value.toString()
                    val cashDiscount = snapshot.child("cashDiscount").value.toString()
                    prevUnpaidBill = snapshot.child("unpaidBill").value.toString().toFloat()
                    val creditDays = snapshot.child("creditDays").value.toString()
                    orderCount = snapshot.child("orderCount").value.toString().toInt()
                    returnCount = snapshot.child("returnCount").value.toString().toInt()

                    for(i in areaList.indices){
                        if(areaList[i].area == areaName)
                            binding.area.setSelection(i)
                    }

                    binding.name.setText(name)
                    binding.addressLine.setText(addressLine)
                    binding.city.setText(city)
                    binding.email.setText(email)
                    binding.contact.setText(contact)
                    binding.gstType.setText(gstType)
                    binding.gstNumber.setText(gstNumber)
                    binding.cashDiscount.setText(cashDiscount)
                    binding.drugLicNo.setText(drugLicNo)
                    binding.fssaiNo.setText(fssaiNo)
                    binding.unpaidBill.setText(prevUnpaidBill.toString())
                    binding.creditDays.setText(creditDays)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Database error",error.message)
                }

            })
    }

    private fun setUpSaveAction(){
        binding.createMaster.setOnClickListener {
            var id = binding.id.text.toString()
            var name = binding.name.text.toString()
            var contact = binding.contact.text.toString()
            var email = binding.email.text.toString()
            var addressLine = binding.addressLine.text.toString()
            var city = binding.city.text.toString()
            var gstType = binding.gstType.text.toString()
            var gstNumber = binding.gstNumber.text.toString()
            var drugLicNo = binding.drugLicNo.text.toString()
            var fssaiNo = binding.fssaiNo.text.toString()
            var cashDiscount = binding.cashDiscount.text.toString()
            var unpaidBill = binding.unpaidBill.text.toString()
            var creditDays = binding.creditDays.text.toString()
            when {
                id.isEmpty() -> {
                    binding.id.error = "Enter name"
                    binding.id.requestFocus()
                }
                name.isEmpty() -> {
                    binding.name.error = "Enter name"
                    binding.name.requestFocus()
                }
                /*contact.isEmpty() ->{
                    binding.contact.error = "Enter contact"
                    binding.contact.requestFocus()
                }
                email.isEmpty()->{
                    binding.email.error = "Enter email"
                    binding.email.requestFocus()
                }*/
                selectedArea.id=="0" ->{
                    Toast.makeText(requireContext(),"Select Area",Toast.LENGTH_SHORT).show()
                }
                /*addressLine.isEmpty() -> {
                    binding.addressLine.error = "Enter address"
                    binding.addressLine.requestFocus()
                }
                city.isEmpty() -> {
                    binding.city.error = "Enter city"
                    binding.city.requestFocus()
                }
                gstType.isEmpty() -> {
                    binding.gstType.error = "Enter gst type"
                    binding.gstType.requestFocus()
                }
                gstNumber.isEmpty() -> {
                    binding.gstNumber.error = "Enter GST Number"
                    binding.gstNumber.requestFocus()
                }
                drugLicNo.isEmpty() -> {
                    binding.drugLicNo.error = "Enter Drug License No"
                    binding.drugLicNo.requestFocus()
                }*/
                fssaiNo.isEmpty() -> {
                    binding.fssaiNo.error = "Enter FSSAI Number"
                    binding.fssaiNo.requestFocus()
                }
                cashDiscount.isEmpty() -> {
                    binding.cashDiscount.error = "Enter cash discount"
                    binding.cashDiscount.requestFocus()
                }
                unpaidBill.isEmpty() -> {
                    binding.unpaidBill.error = "Enter opening balance"
                    binding.unpaidBill.requestFocus()
                }
                creditDays.isEmpty() -> {
                    binding.creditDays.error = "Enter credit days"
                    binding.creditDays.requestFocus()
                }
                else -> {
                    binding.createMaster.isClickable = false
                    binding.loading.visibility = View.VISIBLE
                    saveMaster(id,name,contact,email ,addressLine, city, gstType, gstNumber, drugLicNo,fssaiNo.toLong(),cashDiscount.toFloat(),unpaidBill.toFloat(),creditDays.toInt())
                }
            }
        }
    }

    private fun saveMaster(id:String,name:String,contact:String,email:String,addressLine:String,city:String,gstType:String,gstNumber:String,drugLicNo:String,fssaiNo:Long,cashDiscount:Float,unpaidBill:Float,creditDays:Int){
        val master = Master(id,name,email,contact,selectedArea,addressLine,city,gstType,gstNumber,drugLicNo,fssaiNo,cashDiscount,orderCount,unpaidBill,creditDays,returnCount)
        databaseRef.child(id).setValue(master).addOnSuccessListener {
            if(args.masterId.isEmpty())
                FirebaseDatabase.getInstance().getReference(userId).child("MasterId").setValue(refId.toInt()+1)
                Toast.makeText(requireContext(),"Master saved successfully",Toast.LENGTH_SHORT).show()
                FirebaseDatabase.getInstance().getReference(userId).child("OrderDetails").child("unPaidBill").get().addOnSuccessListener {
                    var amount = it.value.toString().toFloat()
                    amount -= prevUnpaidBill
                    amount += unpaidBill
                    FirebaseDatabase.getInstance().getReference(userId).child("OrderDetails").child("unPaidBill").setValue(amount).addOnSuccessListener {
                        view?.findNavController()?.navigateUp()
                    }.addOnFailureListener {
                        Toast.makeText(requireContext(),"Failed to update amount",Toast.LENGTH_SHORT).show()
                    }
                }


        }.addOnFailureListener {
            binding.createMaster.isClickable = true
            binding.loading.visibility = View.GONE
            Toast.makeText(requireContext(),"Failed to save master",Toast.LENGTH_SHORT).show()
        }
    }

    private fun setUpBackAction(){
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }
}
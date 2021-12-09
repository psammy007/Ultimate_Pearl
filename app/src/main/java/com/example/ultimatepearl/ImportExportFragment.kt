package com.example.ultimatepearl

import android.Manifest.permission.READ_EXTERNAL_STORAGE
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ultimatepearl.databinding.FragmentImportExportBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.FileInputStream
import java.lang.Exception
import java.math.BigDecimal

class ImportExportFragment : Fragment() {

    private lateinit var binding:FragmentImportExportBinding
    private lateinit var databaseMasterRef: DatabaseReference
    private lateinit var databaseProductsRef: DatabaseReference
    private lateinit var firebase: FirebaseDatabase
    private lateinit var databaseAreaRef: DatabaseReference
    private lateinit var areaList: MutableList<Area>
    private lateinit var databaseProductGroupRef: DatabaseReference
    private lateinit var productGroupList: MutableList<ProductGroup>
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private var customerFilePath = ""
    private var productFilePath = ""
    private var masterIdRef:Int = 0
    private var productIdRef:Int = 0
    private var readPermission = false

    private val launcherVar = registerForActivityResult(ActivityResultContracts.GetContent()){
        Log.d("testing",it.toString())
        val uriPathHelper = URIPathHelper()
        val filePath = uriPathHelper.getPath(requireContext(), it)
        Log.d("testing",filePath.toString())
    }

    private val getCustomerFilePath = registerForActivityResult(ActivityResultContracts.GetContent()){
        val uriPathHelper = URIPathHelper()
        customerFilePath = uriPathHelper.getPath(requireContext(), it).toString()
        if(customerFilePath.endsWith(".xlsx"))
            binding.customerFileName.text = customerFilePath.substringAfterLast('/')
        else {
            customerFilePath = ""
            Toast.makeText(requireContext(), "Select a excel file", Toast.LENGTH_SHORT).show()
        }
    }

    private val getProductFilePath = registerForActivityResult(ActivityResultContracts.GetContent()){
        val uriPathHelper = URIPathHelper()
        productFilePath = uriPathHelper.getPath(requireContext(), it).toString()
        if(productFilePath.endsWith(".xlsx"))
            binding.productFileName.text = productFilePath.substringAfterLast('/')
        else{
            productFilePath = ""
            Toast.makeText(requireContext(), "Select a excel file", Toast.LENGTH_SHORT).show()
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()){ isGranted ->
        if(isGranted){
            readPermission = true
            Toast.makeText(requireContext(),"Permissions Granted",Toast.LENGTH_SHORT).show()
        }else{
            Toast.makeText(requireContext(),"Permissions Denied",Toast.LENGTH_SHORT).show()
        }
    }

    /*private val getMasters = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        readMasters(it.data!!.dataString.toString().replace("file://",""))
    }

    private val getProducts = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
        readProducts(it.data!!.dataString.toString().replace("file://",""))
    }*/

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_import_export,container,false)
        return binding.root
    }

    /*override fun onStart() {
        super.onStart()
        initializeVariables()
        getAreas()
        askPermission()
        getProductGroups()
        getReferenceId()
        setBackAction()
        setLinkOpener()
        setFileClearer()
        setFileSelectorForCustomer()
        setFileSelectorForProducts()
        setUploadActions()
    }*/

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        getAreas()
        askPermission()
        getProductGroups()
        getReferenceId()
        setBackAction()
        setLinkOpener()
        setFileClearer()
        setFileSelectorForCustomer()
        setFileSelectorForProducts()
        setUploadActions()
    }

    private fun initializeVariables(){
        areaList = ArrayList()
        productGroupList = ArrayList()
        databaseAreaRef = FirebaseDatabase.getInstance().getReference(userId).child("Areas")
        databaseProductGroupRef = FirebaseDatabase.getInstance().getReference(userId).child("ProductGroups")
    }

    private fun getAreas(){
        databaseAreaRef.get().addOnSuccessListener { snapshot ->
            for(postSnapshot in snapshot.children){
                val key = postSnapshot.key.toString()
                val name = postSnapshot.child("area").value.toString()
                val area = Area(key,name)
                areaList.add(0,area)
            }
            Log.d("areaSize",areaList.size.toString())
        }
    }

    private fun askPermission(){
        if(ContextCompat.checkSelfPermission(requireContext(),READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED)
            readPermission = true
        else
            requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
    }

    private fun getProductGroups() {
        databaseProductGroupRef.get().addOnSuccessListener { snapshot ->
            for (postSnapshot in snapshot.children) {
                val key = postSnapshot.key.toString()
                val name = postSnapshot.child("group").value.toString()
                val productGroup = ProductGroup(key, name)
                productGroupList.add(0, productGroup)
            }
        }
    }

    private fun getReferenceId(){
        FirebaseDatabase.getInstance().getReference(userId).child("MasterId").get().addOnSuccessListener {
            masterIdRef = it.value.toString().toInt()
        }

        FirebaseDatabase.getInstance().getReference(userId).child("ProductId").get().addOnSuccessListener {
            productIdRef = it.value.toString().toInt()
        }
    }

    private fun setBackAction(){
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

    private fun setLinkOpener(){
        binding.openCustRef.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://docs.google.com/spreadsheets/d/1UBB2eEB4o2Gs8LmthHs-alNn0b8reiKqu912-aeaxm4/edit?usp=sharing")
            startActivity(openURL)
        }

        binding.openProdRef.setOnClickListener {
            val openURL = Intent(Intent.ACTION_VIEW)
            openURL.data = Uri.parse("https://docs.google.com/spreadsheets/d/1pUrOUgMB4c7PetqEhMx3jGH4qyscFSBTbHBmS17AWBM/edit?usp=sharing")
            startActivity(openURL)
        }
    }

    private fun setFileSelectorForCustomer() {
        binding.chooseCustomerFile.setOnClickListener {
            if(readPermission) {
                try {
                    getCustomerFilePath.launch("*/*")
                } catch (err: Exception) {
                    Toast.makeText(requireContext(), "Error : " + err.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }else
                askPermission()
        }
    }

    private fun setFileSelectorForProducts(){
        binding.chooseProductFile.setOnClickListener {
            if(readPermission) {
                try {
                    getProductFilePath.launch("*/*")
                } catch (err: Exception) {
                    Toast.makeText(requireContext(), "Error : " + err.message, Toast.LENGTH_SHORT)
                        .show()
                }
            }else
                askPermission()
        }
    }

    private fun setFileClearer(){
        binding.clearCustomerFile.setOnClickListener {
            customerFilePath = ""
            binding.customerFileName.text = "No file is selected"
        }

        binding.clearProductFile.setOnClickListener {
            productFilePath = ""
            binding.productFileName.text = "No file selected"
        }
    }

    private fun setUploadActions(){
        binding.uploadCustomer.setOnClickListener {
            if(customerFilePath.isEmpty())
                Toast.makeText(requireContext(),"Select file",Toast.LENGTH_SHORT).show()
            else {
                binding.uploadCustomer.visibility= View.GONE
                binding.savingCustomers.visibility = View.VISIBLE
                binding.msgC.visibility = View.VISIBLE
                uploadCustomers()
            }
        }

        binding.uploadProducts.setOnClickListener {
            if(productFilePath.isEmpty())
                Toast.makeText(requireContext(),"Select file",Toast.LENGTH_SHORT).show()
            else{
                binding.uploadProducts.visibility= View.GONE
                binding.savingProducts.visibility = View.VISIBLE
                binding.msgP.visibility = View.VISIBLE
                uploadProducts()
            }
        }
    }

    private fun uploadCustomers() {
        val customers:MutableList<Master> = ArrayList()
        try {
            val inputStream = FileInputStream(customerFilePath)
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            val rows = sheet.physicalNumberOfRows
            Log.d("Row",rows.toString())
            for(i in 3..rows){
                try {
                    val row = sheet.getRow(i)
                    val id = "C" + "0".repeat(5 - masterIdRef.toString().length) + masterIdRef.toString()
                    val name = row.getCell(1).toString().trim()
                    var contact = try {
                        BigDecimal(row.getCell(2).toString().trim()).toPlainString()
                    }catch(err:Exception){
                        ""
                    }
                    Log.d("testing","1")
                    val email = try {
                        row.getCell(3).toString().trim()
                    }catch(err:Exception){
                        ""
                    }
                    Log.d("testing","2")
                    var address = try{
                        row.getCell(4).toString().trim()
                    }catch(err:Exception){
                        ""
                    }
                    Log.d("testing","3")
                    val areaName = try{
                        row.getCell(5).toString().trim()
                    }catch(err:Exception){
                        ""
                    }
                    Log.d("testing","4")
                    var area:Area
                    if(areaName.isNotEmpty()) {
                        area = isAreaExist(areaName)
                        if (area.area.isEmpty())
                            area = addArea(areaName)
                    }else
                        area = Area("0","")
                    val city = try{
                        row.getCell(6).toString().trim()
                    }catch(err:Exception){
                        ""
                    }
                    Log.d("testing","6")
                    val gstType = try{
                        row.getCell(7).toString().trim()
                    }catch(err:Exception){
                        ""
                    }
                    Log.d("testing","7")
                    val gstNo = try{
                        row.getCell(8).toString().trim()
                    }catch(err:Exception){
                        ""
                    }
                    Log.d("testing","8")
                    val drugLicNo = try{
                        row.getCell(9).toString().trim()
                    }catch(err:Exception){
                        ""
                    }
                    Log.d("testing","9")
                    val fssaiNo = try{
                        BigDecimal(row.getCell(10).toString().trim()).toPlainString()
                    }catch(err:Exception){
                        "0"
                    }
                    Log.d("testing","10")
                    val cashDis = try{
                        BigDecimal(row.getCell(11).toString().trim()).toPlainString()
                    }catch(err:Exception){
                        "0"
                    }
                    Log.d("testing","11")
                    val openingBalance = try{
                        BigDecimal(row.getCell(12).toString().trim()).toPlainString()
                    }catch(err:Exception){
                        "0"
                    }
                    Log.d("testing","12")
                    val creditDays = try{
                        row.getCell(13).toString().trim().toFloat().toInt()
                    }catch(err:Exception){
                        0
                    }
                    Log.d("testing","13")
                    val master = Master(id,name,email,contact,area,address,city,gstType,gstNo,drugLicNo,fssaiNo.toLong(),cashDis.toFloat(),0,openingBalance.toFloat(),creditDays,0)
                    Log.d("testing","14")
                    customers.add(0,master)
                    Log.d("testing","15 = ${customers.size}")
                    masterIdRef += 1
                    Log.d("testing","16 = ${masterIdRef}")
                }catch (err:Exception){
                    Toast.makeText(requireContext(),"Skipping row $i",Toast.LENGTH_SHORT).show()
                    Log.d("testing",err.toString())
                }
            }
            val mstrRef = FirebaseDatabase.getInstance().getReference(userId).child("Masters")

            var totalOpeningBalance = 0F

            Log.d("length",customers.size.toString())

            for(cstmr in customers){
                totalOpeningBalance += cstmr.unpaidBill
                mstrRef.child(cstmr.id).setValue(cstmr)
            }

            val unpaidBillRef = FirebaseDatabase.getInstance().getReference(userId).child("OrderDetails").child("unPaidBill")
            unpaidBillRef.get().addOnSuccessListener {
                var amount = it.value.toString().toFloat()
                amount += totalOpeningBalance
                amount = String.format("%.2f",amount).toFloat()
                unpaidBillRef.setValue(amount)
            }

            FirebaseDatabase.getInstance().getReference(userId).child("MasterId").setValue(masterIdRef)

            Toast.makeText(requireContext(),"Uploaded customers",Toast.LENGTH_SHORT).show()

        }catch(err:Exception){
            Log.d("Excel err",err.toString())
            Toast.makeText(requireContext(),"Error uploading file. Check your excel file",Toast.LENGTH_SHORT).show()
        }finally {
            binding.customerFileName.text = "No file selected"
            customerFilePath = ""
            binding.msgC.visibility = View.GONE
            binding.savingCustomers.visibility = View.GONE
            binding.uploadCustomer.visibility = View.VISIBLE
        }

    }

    private fun uploadProducts(){
        val productList:MutableList<Product> = ArrayList()
        try{
            val inputStream = FileInputStream(productFilePath)
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            val rows = sheet.lastRowNum
            Log.d("Row",rows.toString())
            for(i in 3..rows){
                try {
                    val row = sheet.getRow(i)
                    val id = "0".repeat(5 - productIdRef.toString().length) + productIdRef.toString()
                    val name = row.getCell(1).toString().trim()
                    val groupName = row.getCell(2).toString().trim()
                    var group = isProductGroupExist(groupName)
                    if (group.group.isEmpty())
                        group = addProductGroup(groupName)
                    val ptr = try{
                        BigDecimal(row.getCell(3).toString()).toPlainString().toFloat()
                    }catch(err:Exception){
                        0F
                    }
                    val mrp = try{
                        BigDecimal(row.getCell(4).toString()).toPlainString().toFloat()
                    }catch(err:Exception){
                        0F
                    }
                    val hsnCode = try{
                        row.getCell(5).toString()
                    }catch(err:Exception){
                        ""
                    }
                    val gst = try{
                        BigDecimal(row.getCell(6).toString()).toPlainString().toFloat()
                    }catch(err:Exception){
                        0F
                    }
                    val stock = try{
                        BigDecimal(row.getCell(7).toString()).toPlainString().toFloat().toInt()
                    }catch(err:Exception){
                        0
                    }
                    val base = try{
                        BigDecimal(row.getCell(8).toString()).toPlainString().toFloat().toInt()
                    }catch(err:Exception){
                        0
                    }
                    val free = try{
                        BigDecimal(row.getCell(9).toString()).toPlainString().toFloat().toInt()
                    }catch(err:Exception){
                        0
                    }
                    val discount = try{
                        BigDecimal(row.getCell(10).toString()).toPlainString().toFloat()
                    }catch(err:Exception){
                        0F
                    }
                    val product = Product(
                        id,
                        name,
                        group,
                        ptr,
                        mrp,
                        gst,
                        hsnCode,
                        stock,
                        Scheme(base, free),
                        discount,
                        0
                    )
                    productList.add(0, product)
                    productIdRef += 1
                }catch(err:Exception){
                    Log.d("Skipping","row $i")
                }
            }

            val prdtRef = FirebaseDatabase.getInstance().getReference(userId).child("Products")

            Log.d("length",productList.size.toString())
            for(product in productList){
                prdtRef.child(product.id).setValue(product)
            }

            FirebaseDatabase.getInstance().getReference(userId).child("ProductId").setValue(productIdRef)
            Toast.makeText(requireContext(),"Uploaded Products",Toast.LENGTH_SHORT).show()

        }catch(err:Exception){
            Log.d("Excel arr",err.toString())
            Toast.makeText(requireContext(),"Error uploading file. Check your excel file",Toast.LENGTH_SHORT).show()
        }finally {
            binding.productFileName.text = "No file selected"
            productFilePath = ""
            binding.msgP.visibility = View.GONE
            binding.savingProducts.visibility = View.GONE
            binding.uploadProducts.visibility = View.VISIBLE
        }
    }

    private fun isAreaExist(areaName:String): Area {
        for(area in areaList){
            if(area.area == areaName)
                return area
        }
        return Area("0","")
    }

    private fun addArea(areaName: String):Area{
        val key = databaseAreaRef.push().key.toString()
        val area = Area(key,areaName)
        databaseAreaRef.child(key).setValue(area)
        areaList.add(0,area)
        return area
    }

    private fun isProductGroupExist(groupName:String):ProductGroup{
        for(productGroup in productGroupList){
            if(productGroup.group == groupName)
                return productGroup
        }
        return ProductGroup("0","")
    }

    private fun addProductGroup(groupName:String):ProductGroup{
        val key = databaseProductGroupRef.push().key.toString()
        val productGroup = ProductGroup(key,groupName)
        databaseProductGroupRef.child(key).setValue(productGroup)
        productGroupList.add(0,productGroup)
        return productGroup
    }



    /*override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeVariables()
        //setUploadMasterAction()
        //setUploadProductsAction()
        setBackAction()
    }

    private fun initializeVariables() {
        firebase = FirebaseDatabase.getInstance()
        databaseMasterRef = firebase.getReference(userId).child("Masters")
        databaseProductsRef = firebase.getReference(userId).child("Products")
        (activity as DrawerAction).lockDrawer()
        firebase.getReference(userId).child("MasterId").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    refMasterId = snapshot.value.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Database error",error.message)
                }

            })
        FirebaseDatabase.getInstance().getReference(userId).child("ProductId").addListenerForSingleValueEvent(
            object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    refProductId = snapshot.value.toString()
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d("Database error",error.message)
                }

            })
    }

    private fun setUploadMasterAction() {
        binding.uploadMaster.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(),READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                binding.uploadMaster.isClickable = true
                binding.uploadingMasters.visibility = View.VISIBLE
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                getMasters.launch(intent)
            } else {
                requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun setUploadProductsAction() {
        binding.uploadProducts.setOnClickListener {
            if (ContextCompat.checkSelfPermission(requireContext(),READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                binding.uploadProducts.isClickable = false
                binding.uploadingProducts.visibility = View.VISIBLE
                val intent = Intent(Intent.ACTION_GET_CONTENT)
                getProducts.launch(intent)
            } else {
                requestPermissionLauncher.launch(READ_EXTERNAL_STORAGE)
            }
        }
    }

    private fun setBackAction(){
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

    private fun readMasters(filePath:String){
        try{
            val inputStream = FileInputStream(filePath)
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            val rows = sheet.physicalNumberOfRows
            for(i in 4..rows){
                try{
                    val row = sheet.getRow(i)
                    val id = "C" + "0".repeat(5-refMasterId.length) + refMasterId
                    val name = row.getCell(1).toString()
                    val addressLine = row.getCell(2).toString()
                    val city = row.getCell(3).toString()
                    val gstType = row.getCell(4).toString()
                    val gstNumber = row.getCell(5).toString()
                    val master = Master(id,name,"","",Area("",""),addressLine,city,gstType,gstNumber,"",0L,0F,0,0F,0,0)
                    refMasterId = (refMasterId.toInt() + 1).toString()
                    databaseMasterRef.child(id).setValue(master).addOnSuccessListener {
                        firebase.getReference(userId).child("MasterId").setValue(refMasterId.toInt())
                    }
                }catch (error:Exception){
                    Log.d("row error",error.toString())
                    Log.d("Master", "skipping $i")
                }
            }
        }catch (error:Exception){
            Log.d("file error",error.toString())
            Toast.makeText(requireContext(),"Error while reading file",Toast.LENGTH_SHORT).show()
        }finally {
            binding.uploadMaster.isClickable = true
            binding.uploadingMasters.visibility = View.GONE
        }
    }

    private fun readProducts(filePath:String){
        try{
            val inputStream = FileInputStream(filePath)
            val workbook = XSSFWorkbook(inputStream)
            val sheet = workbook.getSheetAt(0)
            val rows = sheet.physicalNumberOfRows
            for(i in 3..rows){
                try {
                    val row = sheet.getRow(i)
                    val id = "0".repeat(5-refProductId.length) + refProductId
                    val namePkg = row.getCell(1).toString()
                    val mrp = row.getCell(5).toString().toFloat()
                    val group = row.getCell(2).toString()
                    val ptsRate = row.getCell(3).toString().toFloat()
                    val ptrRate = row.getCell(4).toString().toFloat()
                    val taxRate = row.getCell(6).toString().toFloat()
                    val hsnCode = row.getCell(7).toString()
                    refProductId = (refProductId.toInt() + 1).toString()
                    val product = Product(id, namePkg, ProductGroup("0",group),ptrRate, mrp,taxRate,hsnCode,0,Scheme(0,0),10F,0L)
                    databaseProductsRef.child(id).setValue(product).addOnSuccessListener {
                        firebase.getReference(userId).child("ProductId").setValue(refProductId.toInt())
                    }
                }catch (error:Exception){
                    Log.d("row error",error.toString())
                    Log.d("Product","Skipping $i")
                }
            }9.84654654E
        }catch (error:Exception){
            Log.d("file error",error.toString())
            Toast.makeText(requireContext(),"Error while reading file",Toast.LENGTH_SHORT).show()
        }finally {
            binding.uploadProducts.isClickable = true
            binding.uploadingProducts.visibility = View.GONE
        }
    }*/

    private fun log(index:Int,msg:String){
        Log.d("import $index",msg)
    }

}
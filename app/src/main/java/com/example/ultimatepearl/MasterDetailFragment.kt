package com.example.ultimatepearl

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.ultimatepearl.databinding.FragmentMasterDetailBinding
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MasterDetailFragment : Fragment() {

    private lateinit var binding:FragmentMasterDetailBinding
    lateinit var tabLayout: TabLayout
    lateinit var pager2: ViewPager2
    private lateinit var adapter: MasterDetailFragmentAdapter
    private lateinit var args: MasterDetailFragmentArgs
    private lateinit var database: FirebaseDatabase
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_master_detail,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as MainActivity).lockDrawer()
        initializeVariables()
        setUpPager()
        setEditAction()
        setBackAction()
        binding.viewPager2.offscreenPageLimit = 1
        setUpMasterData()
    }

    private fun setEditAction() {
        binding.edit.setOnClickListener {
            view?.findNavController()?.navigate(MasterDetailFragmentDirections.actionMasterDetailFragmentToCreateMasterFragment(args.id))
        }
    }

    private fun setBackAction(){
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

    private fun initializeVariables() {
        args = MasterDetailFragmentArgs.fromBundle(requireArguments())
        database = FirebaseDatabase.getInstance()
    }

    private fun setUpPager() {
        tabLayout = binding.tabLayout
        pager2 = binding.viewPager2

        val fragmentManager = childFragmentManager
        adapter = MasterDetailFragmentAdapter(fragmentManager, lifecycle,args.id)
        pager2.adapter = adapter

        tabLayout.addTab(tabLayout.newTab().setText("Orders"))
        tabLayout.addTab(tabLayout.newTab().setText("Returns"))


        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab) {
                pager2.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab) {

            }

            override fun onTabReselected(tab: TabLayout.Tab) {

            }

        })

        pager2.registerOnPageChangeCallback(object: ViewPager2.OnPageChangeCallback() {

            override fun onPageSelected(position: Int) {
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }

        })

    }

    private fun setUpMasterData(){
        database.getReference(userId).child("Masters").orderByChild("id").equalTo(args.id).addValueEventListener(object:
            ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child(args.id).child("name").value.toString()
                val designation = snapshot.child(args.id).child("addressLine").value.toString()
                val area = snapshot.child(args.id).child("area").child("area").value.toString()
                var textView = binding.masterName
                textView.text = name
                textView = binding.designation
                textView.text = designation
                textView = binding.mId
                textView.text = args.id + ","
                textView = binding.area
                textView.text = area + ","
            }

            override fun onCancelled(error: DatabaseError) {
                Log.d("Database error",error.message)
            }

        })
    }

}
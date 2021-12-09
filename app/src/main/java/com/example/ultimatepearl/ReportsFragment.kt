package com.example.ultimatepearl

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import androidx.viewpager2.widget.ViewPager2
import com.example.ultimatepearl.databinding.FragmentReportsBinding
import com.google.android.material.tabs.TabLayout

class ReportsFragment : Fragment() {

    private lateinit var binding:FragmentReportsBinding
    private lateinit var fragmentAdapter: FragmentAdapter
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager2: ViewPager2

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_reports,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setBackAction()
        binding.viewPager2.offscreenPageLimit = 2
        setUpPager()
    }

    private fun setBackAction() {
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

    private fun setUpPager(){
        tabLayout = binding.tabLayout
        viewPager2 = binding.viewPager2

        val fragmentManager = childFragmentManager
        fragmentAdapter = FragmentAdapter(fragmentManager,lifecycle)
        viewPager2.adapter = fragmentAdapter

        tabLayout.addTab(tabLayout.newTab().setText("Customers"))
        tabLayout.addTab(tabLayout.newTab().setText("Products"))
        tabLayout.addTab(tabLayout.newTab().setText("Sales"))
        tabLayout.addTab(tabLayout.newTab().setText("Area"))

        tabLayout.addOnTabSelectedListener(object:TabLayout.OnTabSelectedListener{
            override fun onTabSelected(tab: TabLayout.Tab) {
                viewPager2.currentItem = tab.position
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }

        })


        viewPager2.registerOnPageChangeCallback(object:ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                tabLayout.selectTab(tabLayout.getTabAt(position))
            }
        })
    }
}
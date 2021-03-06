package com.example.ultimatepearl

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class FragmentAdapter (fragmentManager: FragmentManager,lifecycle:Lifecycle):
FragmentStateAdapter(fragmentManager,lifecycle){

    private val customerFrag = CustomerFragment()
    private val stockFragment = StockFragment()
    private val salesFragment = SalesFragment()
    private val areaFragment = AreaFragment()


    override fun getItemCount(): Int {
        return 4
    }

    override fun createFragment(position: Int): Fragment {
        return when (position) {
            0 -> customerFrag
            1 -> stockFragment
            2 -> salesFragment
            else -> areaFragment
        }
    }

}
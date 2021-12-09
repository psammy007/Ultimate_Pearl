package com.example.ultimatepearl

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.lifecycle.Lifecycle
import androidx.viewpager2.adapter.FragmentStateAdapter

class MasterDetailFragmentAdapter(fragmentManager: FragmentManager, lifecycle: Lifecycle,id:String):
FragmentStateAdapter(fragmentManager,lifecycle){

    private val ordersFragment = OrdersFragment(id)
    private val returnsFragment = ReturnsFragment(id)

    override fun getItemCount(): Int {
        return 2;
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> ordersFragment
            else  -> returnsFragment
        }
    }

}
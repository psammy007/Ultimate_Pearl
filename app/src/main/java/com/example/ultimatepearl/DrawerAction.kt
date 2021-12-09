package com.example.ultimatepearl

interface DrawerAction {
    fun lockDrawer()
    fun unlockDrawer()
    fun openDrawer()
    fun closeDrawer()
    fun setUpDrawerHeaderInfo()
}
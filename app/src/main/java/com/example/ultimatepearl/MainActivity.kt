package com.example.ultimatepearl

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.databinding.DataBindingUtil
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import androidx.transition.Slide
import com.example.ultimatepearl.databinding.ActivityMainBinding
import com.google.android.material.navigation.NavigationView
import com.google.android.material.transition.platform.SlideDistanceProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class MainActivity : AppCompatActivity(),DrawerAction {

    private lateinit var binding: ActivityMainBinding
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var actionBarDrawerToggle: ActionBarDrawerToggle
    private lateinit var navigationView: NavigationView
    private lateinit var userId:String
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var firebaseDatabase: FirebaseDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DataBindingUtil.setContentView(this,R.layout.activity_main)
        initializeVariables()
        setUpDrawer()
        attachNavControllerToDrawer()
    }

    private fun initializeVariables(){
        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDatabase = FirebaseDatabase.getInstance()
        drawerLayout = binding.drawer
        navigationView = binding.navigationView
    }

    private fun setUpDrawer(){
        actionBarDrawerToggle = object: ActionBarDrawerToggle(this,drawerLayout,R.string.Open,R.string.Close){}
        drawerLayout.addDrawerListener(actionBarDrawerToggle)
        actionBarDrawerToggle.syncState()
    }

    private fun attachNavControllerToDrawer(){
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.navHostFragment)
        val navController = navHostFragment!!.findNavController()
        navigationView.setupWithNavController(navController)
    }

    override fun lockDrawer() {
        /*val lockMode = DrawerLayout.LOCK_MODE_LOCKED_CLOSED
        drawerLayout.setDrawerLockMode(lockMode)*/
    }

    override fun unlockDrawer() {
       /* val unlockMode = DrawerLayout.LOCK_MODE_UNLOCKED
        drawerLayout.setDrawerLockMode(unlockMode)*/
    }

    override fun openDrawer() {
        drawerLayout.open()
    }

    override fun closeDrawer() {
        drawerLayout.close()
    }

    override fun setUpDrawerHeaderInfo() {
        userId = FirebaseAuth.getInstance().currentUser!!.uid
        firebaseDatabase.getReference("Users").orderByChild("id").equalTo(userId)
            .addValueEventListener(object :
                ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val name = snapshot.child(userId).child("name").value.toString()
                    val email = snapshot.child(userId).child("email").value.toString()
                    val header = binding.navigationView.getHeaderView(0)
                    header.findViewById<TextView>(R.id.imageIcon).text = name[0].toString()
                    header.findViewById<TextView>(R.id.name).text = name
                    header.findViewById<TextView>(R.id.email).text = email

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("Database error", error.message)
                }

            })
    }

}
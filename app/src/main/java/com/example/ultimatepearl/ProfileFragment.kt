package com.example.ultimatepearl

import android.app.Dialog
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.core.graphics.drawable.toDrawable
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ultimatepearl.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class ProfileFragment : Fragment() {

    private lateinit var binding:FragmentProfileBinding
    private var userId = FirebaseAuth.getInstance().currentUser!!.uid
    private lateinit var dialog: Dialog

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_profile,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeDialogBox()
        setData()
        setUpLogOutAction()
        setUpBackAction()
        setUpDeleteAction()
        (activity as DrawerAction).lockDrawer()
    }

    private fun initializeDialogBox() {
        dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.name_update_dialog)
        dialog.window?.setBackgroundDrawable(R.drawable.round_border.toDrawable())
        dialog.setCancelable(true)
        val cancel = dialog.findViewById<Button>(R.id.cancel)
        cancel.setOnClickListener {
            dialog.dismiss()
        }
        val delete = dialog.findViewById<Button>(R.id.delete)
        delete.setOnClickListener {
            val database = FirebaseDatabase.getInstance()
            database.run {
                getReference("Users").child(userId).removeValue()
                getReference(userId).removeValue()
            }.addOnSuccessListener {
                FirebaseAuth.getInstance().run {
                    currentUser!!.delete()
                        signOut()
                }
                dialog.dismiss()
                view?.findNavController()?.navigate(ProfileFragmentDirections.actionProfileFragmentToLogInFragment())
            }.addOnFailureListener {
                Toast.makeText(requireContext(),"Failed to delete account",Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setData() {
        FirebaseDatabase.getInstance().getReference("Users").orderByChild("id").equalTo(userId).addValueEventListener(object:ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                val name = snapshot.child(userId).child("name").value.toString()
                val email = snapshot.child(userId).child("email").value.toString()
                binding.imageIcon.text = name[0].toString()
                binding.name.setText(name)
                binding.email.text = email
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("Database error",error.message)
                updateUI()
            }

        })

    }

    private fun updateUI() {
        binding.loading.visibility = View.GONE
        binding.view.visibility = View.VISIBLE
    }

    private fun setUpLogOutAction(){
        binding.logout.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            view?.findNavController()?.navigate(R.id.action_profileFragment_to_logInFragment)
        }
    }

    private fun setUpBackAction(){
        binding.back.setOnClickListener {
            view?.findNavController()?.navigateUp()
        }
    }

    private fun setUpDeleteAction(){
        binding.delete.setOnClickListener {
            dialog.show()
        }
    }

}
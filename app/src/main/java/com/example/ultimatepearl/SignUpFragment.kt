package com.example.ultimatepearl

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ultimatepearl.databinding.FragmentSignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase


class SignUpFragment : Fragment() {

    private lateinit var binding:FragmentSignUpBinding
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: FirebaseDatabase

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_sign_up,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        databaseRef = FirebaseDatabase.getInstance()
        setSignUpAction()
    }

    private fun setSignUpAction() {
        binding.signUp.setOnClickListener {
            val name = binding.name.text.toString()
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            val confirmPassword = binding.confirmPassword.text.toString()

            when {
                name.isEmpty() -> {
                    binding.name.error = "Enter"
                    binding.name.requestFocus()
                }
                email.isEmpty() -> {
                    binding.email.error = "Enter email"
                    binding.email.requestFocus()
                }
                password.length < 6 -> {
                    binding.password.error = "Password length should be minimum 6 char"
                    binding.password.requestFocus()
                }
                confirmPassword.isEmpty() -> {
                    binding.confirmPassword.error = "Renter password"
                    binding.confirmPassword.requestFocus()
                }
                password != confirmPassword -> {
                    binding.confirmPassword.error = "Password mismatch"
                    binding.confirmPassword.requestFocus()
                }
                else -> {
                    binding.signUp.isClickable = false
                    binding.loading.visibility = View.VISIBLE
                    signUp(name, email, password)
                }
            }
        }
    }

    private fun signUp(name:String,email:String,password:String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener {
            val key = firebaseAuth.currentUser!!.uid
            val user = User(key, name, email)
            databaseRef.run {
                getReference("Users").child(user.id).setValue(user)
                getReference(user.id).run {
                    child("MasterId").setValue(1)
                    child("ProductId").setValue(1)
                    child("SalesmanId").setValue(1)
                    child("OrderDetails").child("TotalOrders").setValue(0)
                    child("OrderDetails").child("TotalAmount").setValue(0.0)
                    child("OrderDetails").child("unPaidBill").setValue(0.0)
                }.addOnSuccessListener {
                    Toast.makeText(requireContext(), "User Registration Successful", Toast.LENGTH_SHORT).show()
                    firebaseAuth.signOut()
                    view?.findNavController()?.navigateUp()
                }
            }
        }.addOnFailureListener {
            binding.signUp.isClickable = true
            binding.loading.visibility = View.INVISIBLE
            Toast.makeText(requireContext(), "User Registration Failed", Toast.LENGTH_SHORT).show()
        }
    }

}
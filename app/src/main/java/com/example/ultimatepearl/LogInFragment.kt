package com.example.ultimatepearl

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.navigation.findNavController
import com.example.ultimatepearl.databinding.FragmentLogInBinding
import com.google.firebase.auth.FirebaseAuth

class LogInFragment : Fragment() {

    private lateinit var binding:FragmentLogInBinding
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_log_in,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        if(firebaseAuth.currentUser!=null) {
            (activity as DrawerAction).setUpDrawerHeaderInfo()
            Log.d("login","user logged in")
            view.findNavController().navigate(R.id.action_logInFragment_to_homeFragment)
        }else {
            lockDrawer()
            setLogInAction()
            setSignUpButton()
            setForgotPasswordAction()
        }
    }

    private fun lockDrawer() {
        (activity as DrawerAction).lockDrawer()
    }

    private fun setLogInAction(){
        binding.logIn.setOnClickListener {
            val email = binding.email.text.toString()
            val password = binding.password.text.toString()
            when {
                email.isEmpty() -> {
                    binding.email.error = "Invalid email"
                    binding.email.requestFocus()
                }
                password.length < 6 -> {
                    binding.password.error = "Password length should be minimum 6 char"
                    binding.password.requestFocus()
                }
                else -> {
                    binding.logIn.isClickable = false
                    binding.loading.visibility = View.VISIBLE
                    logIn(email,password)
                }
            }
        }
    }

    private fun logIn(email:String,password:String){
        firebaseAuth.signInWithEmailAndPassword(email,password).addOnSuccessListener {
            Toast.makeText(requireContext(),"Login successful",Toast.LENGTH_LONG).show()
            (activity as DrawerAction).setUpDrawerHeaderInfo()
            (activity as DrawerAction).unlockDrawer()
            view?.findNavController()?.navigate(R.id.action_logInFragment_to_homeFragment)
        }.addOnFailureListener {
            binding.logIn.isClickable = true
            binding.loading.visibility = View.INVISIBLE
            Toast.makeText(requireContext(),"Login failed",Toast.LENGTH_LONG).show()
        }
    }

    private fun setSignUpButton(){
        binding.signUp.setOnClickListener {
            it.findNavController().navigate(R.id.action_logInFragment_to_signUpFragment)
        }
    }

    private fun setForgotPasswordAction() {
        binding.forgotPassword.setOnClickListener {
            view?.findNavController()?.navigate(LogInFragmentDirections.actionLogInFragmentToForgotPasswordFragment())
        }
    }

}
package com.example.ultimatepearl

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import com.example.ultimatepearl.databinding.FragmentForgotPasswordBinding
import com.google.firebase.auth.FirebaseAuth

class ForgotPasswordFragment : Fragment() {

    private lateinit var binding:FragmentForgotPasswordBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        binding = DataBindingUtil.inflate(inflater,R.layout.fragment_forgot_password,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpSendLinkAction()
    }

    private fun setUpSendLinkAction() {
        binding.send.setOnClickListener {
            val email = binding.email.text.toString()
            if(email.isEmpty()){
                binding.email.error = "Enter email"
                binding.email.requestFocus()
            }else{
                binding.send.isClickable = false
                binding.loading.visibility = View.VISIBLE
                FirebaseAuth.getInstance().sendPasswordResetEmail(email).addOnSuccessListener {
                    binding.message.text = "A link to reset password has been sent to above email id. Update password and re-login"
                    binding.loading.visibility = View.GONE
                }.addOnFailureListener {
                    binding.loading.visibility = View.GONE
                    binding.send.isClickable = true
                    binding.email.error = "Invalid email"
                    binding.email.requestFocus()
                }
            }
        }
    }
}
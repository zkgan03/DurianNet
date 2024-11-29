package com.example.duriannet.presentation.account_management.fragments

import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentOTPBinding
import kotlinx.coroutines.launch
import android.widget.Toast
import androidx.navigation.fragment.findNavController
import com.example.duriannet.presentation.account_management.view_models.OTPViewModel

class OTPFragment : Fragment() {

    private var _binding: FragmentOTPBinding? = null
    private val binding get() = _binding!!
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentOTPBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnOtpConfirm.setOnClickListener {
            val otp = binding.edtOtp.text.toString()

            if (otp.isEmpty()) {
                binding.edtOtp.error = "Please enter the OTP"
                return@setOnClickListener
            }

            val sharedPreferences = requireActivity().getSharedPreferences("DurianNetPrefs", Context.MODE_PRIVATE)
            val email = sharedPreferences.getString("email", "") ?: ""

            /*lifecycleScope.launch {
                OTPViewModel.validateOTP(email, otp).collect { state ->
                    if (state.isSuccess) {
                        Toast.makeText(requireContext(), "OTP Verified", Toast.LENGTH_SHORT).show()
                        navController.navigate(R.id.action_otp_to_resetPasswordFragment)
                    } else {
                        Toast.makeText(requireContext(), "Invalid OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }*/
        }


    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
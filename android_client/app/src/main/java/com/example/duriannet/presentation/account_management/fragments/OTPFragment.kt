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
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.duriannet.presentation.account_management.view_models.OTPViewModel
import com.example.duriannet.presentation.account_management.state.OTPValidationState
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class OTPFragment : Fragment() {

    private var _binding: FragmentOTPBinding? = null
    private val binding get() = _binding!!
    private val viewModel: OTPViewModel by viewModels()
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOTPBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("DurianNetPrefs", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "") ?: ""

        binding.btnOtpConfirm.setOnClickListener {
            val otp = binding.edtOtp.text.toString()

            if (otp.isEmpty()) {
                binding.edtOtp.error = "Please enter the OTP"
                return@setOnClickListener
            }

            // Call ViewModel to validate OTP
            viewModel.validateOTP(email, otp)
        }

        // Listen for OTP validation state
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.otpValidationState.collect { state ->
                when (state) {
                    is OTPValidationState.Loading -> {
                        //binding.progressBar.visibility = View.VISIBLE // Show loading
                        binding.btnOtpConfirm.isEnabled = false
                    }
                    is OTPValidationState.Success -> {
                        //binding.progressBar.visibility = View.GONE // Hide loading
                        binding.btnOtpConfirm.isEnabled = true
                        Toast.makeText(requireContext(), "OTP validated successfully", Toast.LENGTH_SHORT).show()
                        navController.navigate(R.id.action_otp_to_resetPasswordFragment) // Navigate to reset password
                    }
                    is OTPValidationState.Error -> {
                        //binding.progressBar.visibility = View.GONE // Hide loading
                        binding.btnOtpConfirm.isEnabled = true
                        Toast.makeText(requireContext(), state.message, Toast.LENGTH_SHORT).show()
                    }
                    else -> {
                        //binding.progressBar.visibility = View.GONE // Hide loading
                        binding.btnOtpConfirm.isEnabled = true
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


package com.example.duriannet.presentation.account_management.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentResetPasswordBinding
import com.example.duriannet.presentation.account_management.view_models.ResetPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ResetPasswordViewModel by viewModels()
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("DurianNetPrefs", Context.MODE_PRIVATE)
        val email = sharedPreferences.getString("email", "") ?: ""

        binding.btnResetPassword.setOnClickListener {
            val newPassword = binding.edtRpNewPassword.text.toString()
            val confirmPassword = binding.edtRpConfPassword.text.toString()

            // ** Start Validation **
            if (newPassword.isEmpty()) {
                binding.edtRpNewPassword.error = "New password cannot be empty"
                return@setOnClickListener
            }

            val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&#_])[A-Za-z\\d@\$!%*?&#_]{8,}$"
            if (!newPassword.matches(passwordPattern.toRegex())) {
                binding.edtRpNewPassword.error = "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                binding.edtRpConfPassword.error = "Confirm password cannot be empty"
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                binding.edtRpConfPassword.error = "Passwords do not match"
                return@setOnClickListener
            }
            // ** End Validation **

            viewModel.resetPassword(newPassword, confirmPassword, email)
        }

        lifecycleScope.launch {
            viewModel.resetPasswordState.collect { state ->
                if (state.isPasswordReset) {
                    Toast.makeText(requireContext(), "Password reset successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.action_reset_password_to_login)
                } else if (state.error.isNotEmpty()) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.lblRpToLogin.setOnClickListener {
            navController.navigate(R.id.action_reset_password_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

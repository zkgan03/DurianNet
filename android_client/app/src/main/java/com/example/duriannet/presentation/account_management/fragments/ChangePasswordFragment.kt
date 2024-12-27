package com.example.duriannet.presentation.account_management.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentChangePasswordBinding
import com.example.duriannet.presentation.account_management.view_models.ChangePasswordViewModel
import com.example.duriannet.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect

@AndroidEntryPoint
class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ChangePasswordViewModel by viewModels()
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChangePasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSavePassword.setOnClickListener {
            val currentPassword = binding.edtCurrentPassword.text.toString()
            val newPassword = binding.edtNewPassword.text.toString()
            val confirmPassword = binding.edtConfPassword.text.toString()

            if (currentPassword.isEmpty()) {
                binding.edtCurrentPassword.error = "Current password cannot be empty"
                return@setOnClickListener
            }

            if (newPassword.isEmpty()) {
                binding.edtNewPassword.error = "New password cannot be empty"
                return@setOnClickListener
            }

            if (!isValidPassword(newPassword)) {
                binding.edtNewPassword.error = "Password must be at least 12 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                binding.edtConfPassword.error = "Confirm password cannot be empty"
                return@setOnClickListener
            }

            if (newPassword != confirmPassword) {
                requireContext().showToast("New Password and Confirm Password do not match")
                return@setOnClickListener
            }

            viewModel.changePassword(currentPassword, newPassword)
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.serverMessage.collect { message ->
                message?.let {
                    requireContext().showToast(it)
                    viewModel.clearMessage()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.changePasswordState.collect { state ->
                if (state.isPasswordChanged) {
                    requireContext().showToast("Password changed successfully")
                    navController.navigate(R.id.action_change_password_to_profile)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern = "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&#_])[A-Za-z\\d@\$!%*?&#_]{12,}\$"
        return password.matches(passwordPattern.toRegex())
    }
}


package com.example.duriannet.presentation.account_management.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.duriannet.databinding.FragmentResetPasswordBinding
import com.example.duriannet.presentation.account_management.view_models.ResetPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val resetPasswordViewModel: ResetPasswordViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnResetPassword.setOnClickListener {
            val newPassword = binding.edtRpNewPassword.text.toString()
            val confirmPassword = binding.edtRpConfPassword.text.toString()
            resetPasswordViewModel.resetPassword(newPassword, confirmPassword)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            resetPasswordViewModel.resetPasswordState.collect { state ->
                if (state.isPasswordReset) {
                    Toast.makeText(requireContext(), "Password Reset Successful", Toast.LENGTH_SHORT).show()
                    // Navigate to the next screen
                } else if (state.error.isNotEmpty()) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
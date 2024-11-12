package com.example.duriannet.presentation.account_management.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.duriannet.databinding.FragmentChangePasswordBinding
import com.example.duriannet.presentation.account_management.view_models.ChangePasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ChangePasswordFragment : Fragment() {

    private var _binding: FragmentChangePasswordBinding? = null
    private val binding get() = _binding!!

    private val changePasswordViewModel: ChangePasswordViewModel by viewModels()

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
            val newPassword = binding.edtNewPassword.text.toString()
            val confirmPassword = binding.edtConfPassword.text.toString()
            changePasswordViewModel.changePassword(newPassword, confirmPassword)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            changePasswordViewModel.changePasswordState.collect { state ->
                if (state.isPasswordChanged) {
                    Toast.makeText(requireContext(), "Password Changed Successfully", Toast.LENGTH_SHORT).show()
                    // Navigate to the next screen or perform other actions
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
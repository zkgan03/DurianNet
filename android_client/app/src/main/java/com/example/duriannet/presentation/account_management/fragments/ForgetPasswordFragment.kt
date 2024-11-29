package com.example.duriannet.presentation.account_management.fragments

import android.content.Context
import android.os.Bundle
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentForgetPasswordBinding
import com.example.duriannet.presentation.account_management.view_models.ForgetPasswordViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgetPasswordFragment : Fragment() {

    private var _binding: FragmentForgetPasswordBinding? = null
    private val binding get() = _binding!!
    private val viewModel: ForgetPasswordViewModel by viewModels()
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentForgetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("DurianNetPrefs", Context.MODE_PRIVATE)

        binding.btnFp.setOnClickListener {
            val email = binding.edtFpEmail.text.toString()

            // Input validation
            if (email.isEmpty()) {
                binding.edtFpEmail.error = "Email cannot be empty"
                return@setOnClickListener
            }
            if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.edtFpEmail.error = "Enter a valid email address"
                return@setOnClickListener
            }

            viewModel.sendResetEmail(email)

            // Save email to SharedPreferences for use in ResetPasswordFragment
            sharedPreferences.edit().putString("email", email).apply()
        }

        lifecycleScope.launch {
            viewModel.forgetPasswordState.collect { state ->
                if (state.isEmailSent) {
                    Toast.makeText(requireContext(), "Reset email sent", Toast.LENGTH_SHORT).show()
                    //navController.navigate(R.id.action_forget_password_to_reset_password)
                } else if (state.error.isNotEmpty()) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.lblFpToLogin.setOnClickListener {
            navController.navigate(R.id.action_forget_password_to_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

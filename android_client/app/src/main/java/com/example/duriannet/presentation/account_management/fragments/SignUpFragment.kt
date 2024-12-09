package com.example.duriannet.presentation.account_management.fragments

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
import com.example.duriannet.databinding.FragmentSignUpBinding
import com.example.duriannet.presentation.account_management.state.SignUpState
import com.example.duriannet.presentation.account_management.view_models.SignUpViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment() {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignUpViewModel by viewModels()
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSignUp.setOnClickListener {
            val username = binding.edtSignUpUsername.text.toString()
            val email = binding.edtSignUpEmail.text.toString()
            val password = binding.edtSignUpPassword.text.toString()
            val confirmPassword = binding.edtSignUpConfPassword.text.toString()

            // Validate input fields
            if (username.isEmpty()) {
                binding.edtSignUpUsername.error = "Username cannot be empty"
                return@setOnClickListener
            }

            if (username.length < 5) {
                binding.edtSignUpUsername.error = "Username must be at least 5 characters long"
                return@setOnClickListener
            }

            if (email.isEmpty()) {
                binding.edtSignUpEmail.error = "Email cannot be empty"
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.edtSignUpEmail.error = "Enter a valid email address"
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.edtSignUpPassword.error = "Password cannot be empty"
                return@setOnClickListener
            }

            if (!isValidPassword(password)) {
                binding.edtSignUpPassword.error = "Password must be at least 8 characters long and include at least one uppercase letter, one lowercase letter, one number, and one special character."
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                binding.edtSignUpConfPassword.error = "Confirm password cannot be empty"
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                binding.edtSignUpConfPassword.error = "Passwords do not match"
                return@setOnClickListener
            }

            viewModel.signUp(username, email, password, confirmPassword)
        }

        lifecycleScope.launch {
            viewModel.signUpState.collect { state ->
                if (state.isRegistered) {
                    Toast.makeText(requireContext(), "Registration successful", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.action_sign_up_to_login)
                } else if (state.error.isNotEmpty()) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.lblSignUpToLogin.setOnClickListener {
            navController.navigate(R.id.action_sign_up_to_login)
        }
    }

    private fun isValidPassword(password: String): Boolean {
        val passwordPattern =
            "^(?=.*[A-Z])(?=.*[a-z])(?=.*\\d)(?=.*[@\$!%*?&#_])[A-Za-z\\d@\$!%*?&#_]{8,}$"
        return password.matches(passwordPattern.toRegex())
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

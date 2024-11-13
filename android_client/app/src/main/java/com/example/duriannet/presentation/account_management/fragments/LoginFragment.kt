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
import com.example.duriannet.databinding.FragmentLoginBinding
import com.example.duriannet.presentation.account_management.view_models.LoginViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!

    private val loginViewModel: LoginViewModel by viewModels()
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnLogin.setOnClickListener {
            val username = binding.edtLoginUsername.text.toString()
            val password = binding.edtLoginPassword.text.toString()
            loginViewModel.login(username, password)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            loginViewModel.loginState.collect { state ->
                if (state.isLoggedIn) {
                    Toast.makeText(requireContext(), "Login Successful", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.action_login_to_profile)
                } else if (state.error.isNotEmpty()) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                }
            }
        }

        binding.lblLoginToSignUp1.setOnClickListener {
            navController.navigate(R.id.action_login_to_sign_up)
        }

        binding.lblForgetPassword.setOnClickListener {
            navController.navigate(R.id.action_login_to_forget_password)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
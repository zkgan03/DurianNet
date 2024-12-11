package com.example.duriannet.presentation.account_management.fragments

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentLoginBinding
import com.example.duriannet.presentation.account_management.view_models.LoginViewModel
import com.example.duriannet.utils.showToast
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LoginViewModel by viewModels()
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

        val sharedPreferences = requireActivity().getSharedPreferences("DurianNetPrefs", Context.MODE_PRIVATE)

        // Check if token exists for auto-login
        val token = sharedPreferences.getString("token", null)
        val username = sharedPreferences.getString("username", null)

        if (token != null && username != null) {
            lifecycleScope.launch {
                try {
                    // Call the API to get all users
                    val users = viewModel.getAllUsers()

                    // Check if the user exists and has Active status
                    val currentUser = users?.find { it.username == username }
                    if (currentUser != null && currentUser.status == "Active") {
                        navController.navigate(R.id.action_login_to_durian_profile)
                    } else {
                        requireContext().showToast("Account is not active. Please contact support.")
                    }
                } catch (e: Exception) {
                    Log.e("LoginFragment", "Error checking user status: ${e.message}")
                    requireContext().showToast("Error validating user status. Please try again.")
                }
            }
            return
        }

        binding.btnLogin.setOnClickListener {
            val username = binding.edtLoginUsername.text.toString()
            val password = binding.edtLoginPassword.text.toString()
            val rememberMe = binding.chkRememberMe.isChecked

            if (username.isEmpty()) {
                binding.edtLoginUsername.error = "Username cannot be empty"
                return@setOnClickListener
            }
            if (password.isEmpty()) {
                binding.edtLoginPassword.error = "Password cannot be empty"
                return@setOnClickListener
            }

            viewModel.login(username, password)

            observeViewModel(sharedPreferences, rememberMe)
        }

        binding.lblForgetPassword.setOnClickListener {
            navController.navigate(R.id.action_login_to_forget_password)
        }

        binding.lblLoginToSignUp1.setOnClickListener {
            navController.navigate(R.id.action_login_to_sign_up)
        }
    }

    private fun observeViewModel(sharedPreferences: SharedPreferences, rememberMe: Boolean) {
        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.serverMessage.collect { message ->
                message?.let {
                    requireContext().showToast(it)
                    viewModel.clearMessage()
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launchWhenStarted {
            viewModel.loginState.collect { state ->
                if (state.isLoggedIn) {
                    // Ensure the navigation action is only performed if still on LoginFragment
                    if (navController.currentDestination?.id == R.id.loginFragment) {
                        val editor = sharedPreferences.edit()
                        editor.putString("username", state.username)
                        if (rememberMe) {
                            editor.putString("token", state.token)
                        }
                        editor.apply()

                        navController.navigate(R.id.action_login_to_durian_profile)
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


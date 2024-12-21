package com.example.duriannet.presentation.account_management.fragments

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentProfileBinding
import com.example.duriannet.presentation.account_management.adapter.FavoriteDurianAdapter
import com.example.duriannet.presentation.account_management.view_models.ProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val profileViewModel: ProfileViewModel by viewModels()
    private lateinit var adapter: FavoriteDurianAdapter
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        parentFragmentManager.setFragmentResultListener("favorite_updated", this) { _, _ ->
            val sharedPreferences =
                requireActivity().getSharedPreferences("DurianNetPrefs", Context.MODE_PRIVATE)
            val username = sharedPreferences.getString("username", "") ?: ""

            if (username.isNotEmpty()) {
                profileViewModel.loadProfile(username) // Reload the latest profile data
            }
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val logoutReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                // Handle logout and navigate to login screen
                findNavController().navigate(R.id.loginFragment)
            }
        }

        LocalBroadcastManager.getInstance(requireContext())
            .registerReceiver(logoutReceiver, IntentFilter("com.example.duriannet.LOGOUT"))

        val sharedPreferences =
            requireActivity().getSharedPreferences("DurianNetPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""

        if (username.isNotEmpty()) {
            profileViewModel.loadProfile(username) // Ensure the latest data is fetched
        }

        // Initialize adapter with click listener
        adapter = FavoriteDurianAdapter { durianId ->
            // Navigate to DurianProfileDetailsFragment using the durianId
            val action = ProfileFragmentDirections.actionProfileToDurianProfileDetails(durianId)
            navController.navigate(action)
        }

        binding.rvProfileFavoriteDurian.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProfileFavoriteDurian.adapter = adapter

        if (username.isNotEmpty()) {
            profileViewModel.loadProfile(username)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.profileState.collect { state ->
                binding.progressBar.visibility = if (state.loading) View.VISIBLE else View.GONE

                if (state.error.isNotEmpty()) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                } else {
                    binding.txtProfileUsername.text = state.username
                    binding.txtProfileFullname.text = state.fullName
                    binding.txtProfileEmail.text = state.email
                    binding.txtProfilePhoneNumber.text = state.phoneNumber

                    Glide.with(requireContext())
                        .load(state.profileImageUrl)
                        .placeholder(R.drawable.unknownuser)
                        .error(R.drawable.unknownuser)
                        .centerCrop()
                        .into(binding.ivProfile)

                    adapter.submitList(state.favoriteDurians)
                }
            }
        }


        binding.toolbarChangePassword.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_edit_profile -> {
                    navController.navigate(R.id.editProfileFragment)
                    true
                }
                R.id.action_change_password -> {
                    navController.navigate(R.id.changePasswordFragment)
                    true
                }
                R.id.delete_account -> {
                    showConfirmationDialog(
                        "Delete Account",
                        "Are you sure you want to delete your account? This action cannot be undone."
                    ) {
                        lifecycleScope.launch {
                            val result = profileViewModel.deleteAccount(username)
                            if (result.isSuccess) {
                                Toast.makeText(
                                    requireContext(),
                                    "Account deleted successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                sharedPreferences.edit().clear().apply() // Clear all shared preferences
                                navController.navigate(R.id.loginFragment) // Navigate to login
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    result.exceptionOrNull()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    true
                }
                R.id.logout -> {
                    showConfirmationDialog(
                        "Logout",
                        "Are you sure you want to log out?"
                    ) {
                        lifecycleScope.launch {
                            val result = profileViewModel.logout()
                            if (result.isSuccess) {
                                Toast.makeText(
                                    requireContext(),
                                    "Logged out successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                                sharedPreferences.edit().clear().apply() // Clear all shared preferences
                                navController.navigate(R.id.loginFragment) // Navigate to login
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    result.exceptionOrNull()?.message,
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    true
                }
                else -> false
            }
        }

        binding.edtFdIcon.setOnClickListener {
            try {
                navController.navigate(R.id.favoriteDurianFragment)
            } catch (e: IllegalArgumentException) {
                Toast.makeText(
                    requireContext(),
                    "Navigation error: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun showConfirmationDialog(title: String, message: String, onConfirm: () -> Unit) {
        AlertDialog.Builder(requireContext())
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton("Yes") { _, _ ->
                onConfirm()
            }
            .setNegativeButton("No", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
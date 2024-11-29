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
import androidx.recyclerview.widget.LinearLayoutManager
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("DurianNetPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""

        adapter = FavoriteDurianAdapter()
        binding.rvProfileFavoriteDurian.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProfileFavoriteDurian.adapter = adapter

        if (username.isNotEmpty()) {
            profileViewModel.loadProfile(username)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.profileState.collect { state ->
                if (state.error.isNotEmpty()) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                } else {
                    binding.txtProfileUsername.text = state.username
                    binding.txtProfileFullname.text = state.fullName
                    binding.txtProfileEmail.text = state.email
                    binding.txtProfilePhoneNumber.text = state.phoneNumber
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
                    // Handle delete account logic
                    Toast.makeText(requireContext(), "Delete Account clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.logout -> {
                    // Handle logout logic
                    Toast.makeText(requireContext(), "Logout clicked", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }


        binding.edtFdIcon.setOnClickListener {
            try {
                navController.navigate(R.id.favoriteDurianFragment)
            } catch (e: IllegalArgumentException) {
                Toast.makeText(requireContext(), "Navigation error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
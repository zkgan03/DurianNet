package com.example.duriannet.presentation.account_management.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        val adapter = FavoriteDurianAdapter()
        binding.rvProfileFavoriteDurian.layoutManager = LinearLayoutManager(requireContext())
        binding.rvProfileFavoriteDurian.adapter = adapter

        profileViewModel.loadProfile()

        viewLifecycleOwner.lifecycleScope.launch {
            profileViewModel.profileState.collect { state ->
                if (state.error.isNotEmpty()) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                } else {
                    binding.txtProfileUsername.text = state.username
                    binding.txtProfileFullname.text = state.fullname
                    binding.txtProfileEmail.text = state.email
                    binding.txtProfilePhoneNumber.text = state.phoneNumber
                    adapter.submitList(state.favoriteDurians)
                }
            }
        }

        binding.toolbarChangePassword.setOnMenuItemClickListener { item: MenuItem ->
            when (item.itemId) {
                R.id.edit_profile -> {
                    findNavController().navigate(R.id.action_profile_to_edit_profile)
                    true
                }
                R.id.change_password -> {
                    findNavController().navigate(R.id.action_profile_to_change_password)
                    true
                }
                else -> false
            }
        }

        binding.edtFdIcon.setOnClickListener {
            findNavController().navigate(R.id.action_profile_to_favorite_durian)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
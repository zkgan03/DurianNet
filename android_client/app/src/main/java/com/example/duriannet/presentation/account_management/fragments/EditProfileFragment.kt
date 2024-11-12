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
import com.example.duriannet.databinding.FragmentEditProfileBinding
import com.example.duriannet.presentation.account_management.view_models.EditProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val editProfileViewModel: EditProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        editProfileViewModel.loadProfile()

        binding.btnEdtProfileSave.setOnClickListener {
            val username = binding.edtProfileUsername.text.toString()
            val fullname = binding.edtProfileFullname.text.toString()
            val email = binding.edtProfileEmail.text.toString()
            val phoneNumber = binding.edtProfilePhoneNumber.text.toString()
            editProfileViewModel.updateProfile(username, fullname, email, phoneNumber)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            editProfileViewModel.editProfileState.collect { state ->
                if (state.isProfileUpdated) {
                    Toast.makeText(requireContext(), "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                    // Navigate to profile fragment
                    findNavController().navigate(R.id.action_edit_profile_to_profile)
                } else if (state.error.isNotEmpty()) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                } else {
                    binding.edtProfileUsername.setText(state.username)
                    binding.edtProfileFullname.setText(state.fullname)
                    binding.edtProfileEmail.setText(state.email)
                    binding.edtProfilePhoneNumber.setText(state.phoneNumber)
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
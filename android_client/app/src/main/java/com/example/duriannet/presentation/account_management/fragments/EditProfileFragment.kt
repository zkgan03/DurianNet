package com.example.duriannet.presentation.account_management.fragments

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentEditProfileBinding
import com.example.duriannet.presentation.account_management.view_models.EditProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

@AndroidEntryPoint
class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: EditProfileViewModel by viewModels()
    private val navController by lazy { findNavController() }
    private var profileImagePath: String? = null

    // Register an ActivityResultLauncher to handle the image selection
    private val selectImageLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val selectedImageUri = result.data!!.data
            selectedImageUri?.let { uri ->
                binding.ivProfile.setImageURI(uri)
                profileImagePath = getPathFromUri(uri)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("DurianNetPrefs", Context.MODE_PRIVATE)
        val username = sharedPreferences.getString("username", "") ?: ""
        if (username.isNotEmpty()) {
            viewModel.loadProfile(username)
        }

        // Handle profile picture click
        binding.ivProfile.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            selectImageLauncher.launch(intent)
        }

        binding.btnEdtProfileSave.setOnClickListener {
            val fullName = binding.edtProfileFullname.text.toString()
            val email = binding.edtProfileEmail.text.toString()
            val phoneNumber = binding.edtProfilePhoneNumber.text.toString()

            // Validate all fields (excluding profile picture)
            when {
                fullName.isEmpty() -> {
                    binding.edtProfileFullname.error = "Full name is required"
                    return@setOnClickListener
                }
                fullName == "-" -> {
                    binding.edtProfileFullname.error = "Full name cannot be '-'"
                    return@setOnClickListener
                }
                email.isEmpty() -> {
                    binding.edtProfileEmail.error = "Email is required"
                    return@setOnClickListener
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    binding.edtProfileEmail.error = "Enter a valid email address"
                    return@setOnClickListener
                }
                phoneNumber.isEmpty() -> {
                    binding.edtProfilePhoneNumber.error = "Phone number is required"
                    return@setOnClickListener
                }
                !phoneNumber.matches("^[0-9]{10,11}$".toRegex()) -> {
                    binding.edtProfilePhoneNumber.error = "Enter a valid 10- or 11-digit phone number"
                    return@setOnClickListener
                }
            }

            if (profileImagePath.isNullOrEmpty()) {
                // No new image, update the profile without updating the image
                viewModel.updateUserWithoutImage(
                    username = username,
                    fullName = fullName,
                    email = email,
                    phoneNumber = phoneNumber
                )
            } else {
                // A new image is selected, upload it
                val file = File(profileImagePath!!)
                val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
                val profilePicturePart = MultipartBody.Part.createFormData("profilePicture", file.name, requestFile)

                viewModel.updateProfileWithImage(
                    username = username,
                    fullName = fullName,
                    email = email,
                    phoneNumber = phoneNumber,
                    profilePicture = profilePicturePart
                )
            }
        }



        lifecycleScope.launchWhenStarted {
            viewModel.editProfileState.collect { state ->
                if (state.isProfileUpdated) {
                    Toast.makeText(context, "Profile Updated Successfully", Toast.LENGTH_SHORT).show()
                    navController.navigate(R.id.action_edit_profile_to_profile)
                } else if (state.error.isNotEmpty()) {
                    Toast.makeText(context, state.error, Toast.LENGTH_SHORT).show()
                } else {
                    binding.edtProfileFullname.setText(state.fullName)
                    binding.edtProfileEmail.setText(state.email)
                    binding.edtProfilePhoneNumber.setText(state.phoneNumber)
                    /*state.profilePicture?.let { profilePicturePath ->
                        binding.ivProfile.setImageURI(Uri.parse(profilePicturePath))*/
                    state.profilePicture?.let { profilePicturePath ->
                        Glide.with(requireContext())
                            .load(profilePicturePath)
                            .placeholder(R.drawable.unknownuser) // Fallback if the image is loading
                            .error(R.drawable.unknownuser) // Fallback if there's an error
                            .centerCrop()
                            .into(binding.ivProfile)
                    }


                }
            }
        }
    }

    private fun getPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor = requireContext().contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            if (it.moveToFirst()) {
                val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                return it.getString(columnIndex)
            }
        }
        return null
    }

    private fun uploadProfileImage(username: String, imagePath: String) {
        val file = File(imagePath)
        val requestFile = file.asRequestBody("image/jpeg".toMediaTypeOrNull())
        val multipartBody = MultipartBody.Part.createFormData("profilePicture", file.name, requestFile)

        val fullName = binding.edtProfileFullname.text.toString()
        val email = binding.edtProfileEmail.text.toString()
        val phoneNumber = binding.edtProfilePhoneNumber.text.toString()

        viewModel.updateProfileWithImage(username, fullName, email, phoneNumber, multipartBody)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

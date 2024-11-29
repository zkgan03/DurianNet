package com.example.duriannet.presentation.account_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.remote.dtos.request.user.UpdateUserProfileRequestDto
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.presentation.account_management.state.EditProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _editProfileState = MutableStateFlow(EditProfileState())
    val editProfileState: StateFlow<EditProfileState> = _editProfileState

    fun loadProfile(username: String) {
        viewModelScope.launch {
            val result = userRepository.getProfile(username)
            if (result.isSuccess) {
                val profile = result.getOrNull()
                _editProfileState.value = profile?.let {
                    _editProfileState.value.copy(
                        username = it.userName,
                        fullName = it.fullName,
                        email = it.email,
                        phoneNumber = it.phoneNumber,
                        profilePicture = it.profilePicture
                    )
                } ?: EditProfileState(error = "Profile not found")
            } else {
                _editProfileState.value = EditProfileState(error = result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun updateProfile(username: String, fullName: String, email: String, phoneNumber: String, profilePicture: String?) {
        viewModelScope.launch {
            val request = UpdateUserProfileRequestDto(
                fullName = fullName,
                email = email,
                phoneNumber = phoneNumber,
                profilePicture = profilePicture
            )
            val result = userRepository.updateProfile(username, request)
            if (result.isSuccess) {
                _editProfileState.value = _editProfileState.value.copy(isProfileUpdated = true)
            } else {
                _editProfileState.value = _editProfileState.value.copy(
                    error = result.exceptionOrNull()?.message ?: "Unknown error"
                )
            }
        }
    }
}

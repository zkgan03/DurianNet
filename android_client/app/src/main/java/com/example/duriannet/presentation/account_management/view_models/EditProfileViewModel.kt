package com.example.duriannet.presentation.account_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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

    fun loadProfile() {
        viewModelScope.launch {
            val result = userRepository.getProfile()
            if (result.isSuccess) {
                val profile = result.getOrNull()
                _editProfileState.value = profile?.let {
                    EditProfileState(
                        username = it.username,
                        fullname = it.fullname,
                        email = it.email,
                        phoneNumber = it.phoneNumber
                    )
                } ?: EditProfileState(error = "Profile not found")
            } else {
                _editProfileState.value = EditProfileState(error = result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }

    fun updateProfile(username: String, fullname: String, email: String, phoneNumber: String) {
        viewModelScope.launch {
            val result = userRepository.updateProfile(username, fullname, email, phoneNumber)
            if (result.isSuccess) {
                _editProfileState.value = EditProfileState(isProfileUpdated = true)
            } else {
                _editProfileState.value = EditProfileState(error = result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}
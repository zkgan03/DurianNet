package com.example.duriannet.presentation.account_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.models.Profile
import com.example.duriannet.presentation.account_management.state.ProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    fun loadProfile() {
        viewModelScope.launch {
            val result = userRepository.getProfile()
            if (result.isSuccess) {
                val profile = result.getOrNull()
                _profileState.value = profile?.let {
                    ProfileState(
                        username = it.username,
                        fullname = it.fullname,
                        email = it.email,
                        phoneNumber = it.phoneNumber,
                        favoriteDurians = it.favoriteDurians
                    )
                } ?: ProfileState(error = "Profile not found")
            } else {
                _profileState.value = ProfileState(error = result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}
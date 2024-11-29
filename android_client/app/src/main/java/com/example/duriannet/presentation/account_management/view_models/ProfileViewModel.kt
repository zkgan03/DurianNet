package com.example.duriannet.presentation.account_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.data.repository.durian_dictionary.DurianRepository
import com.example.duriannet.presentation.account_management.state.ProfileState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val durianRepository: DurianRepository
) : ViewModel() {

    private val _profileState = MutableStateFlow(ProfileState())
    val profileState: StateFlow<ProfileState> = _profileState

    fun loadProfile(username: String) {
        viewModelScope.launch {
            try {
                val profileResult = userRepository.getProfile(username)
                if (profileResult.isSuccess) {
                    val profile = profileResult.getOrNull()
                    if (profile != null) {
                        _profileState.value = _profileState.value.copy(
                            username = profile.userName,
                            fullName = profile.fullName,
                            email = profile.email,
                            phoneNumber = profile.phoneNumber
                        )
                    } else {
                        _profileState.value = _profileState.value.copy(error = "Profile not found")
                    }
                } else {
                    _profileState.value = _profileState.value.copy(error = "Failed to load profile")
                }

                val favoriteResult = durianRepository.getFavoriteDurians(username)
                if (favoriteResult.isSuccess) {
                    val favorites = favoriteResult.getOrNull()?.mapNotNull { it.durianName } ?: emptyList()
                    _profileState.value = _profileState.value.copy(favoriteDurians = favorites)
                } else {
                    _profileState.value = _profileState.value.copy(error = "Failed to load favorite durians")
                }
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(error = e.message ?: "Unknown error")
            }
        }
    }

}

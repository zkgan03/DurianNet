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
        _profileState.value = _profileState.value.copy(loading = true) // Set loading to true
        viewModelScope.launch {
            try {
                val profileResult = userRepository.getProfile(username)
                val favoriteResult = durianRepository.getFavoriteDurians(username)

                if (profileResult.isSuccess && favoriteResult.isSuccess) {
                    val profile = profileResult.getOrNull()
                    val favorites = favoriteResult.getOrNull()?.map { it.durianId to it.durianName } ?: emptyList()

                    val baseUrl = "http://10.0.2.2:5176"
                    val profileImageUrl = profile?.profilePicture?.let {
                        if (it.startsWith("http")) it else "$baseUrl${if (it.startsWith("/")) it else "/$it"}"
                    } ?: "$baseUrl/defaultProfileImage.png"

                    _profileState.value = _profileState.value.copy(
                        username = profile?.userName.orEmpty(),
                        fullName = profile?.fullName.orEmpty(),
                        email = profile?.email.orEmpty(),
                        phoneNumber = profile?.phoneNumber.orEmpty(),
                        profileImageUrl = profileImageUrl,
                        favoriteDurians = favorites,
                        loading = false // Set loading to false after success
                    )
                } else {
                    _profileState.value = _profileState.value.copy(
                        error = "Failed to load profile or favorites.",
                        loading = false // Set loading to false after failure
                    )
                }
            } catch (e: Exception) {
                _profileState.value = _profileState.value.copy(
                    error = e.message ?: "Unknown error.",
                    loading = false // Set loading to false after exception
                )
            }
        }
    }


    suspend fun deleteAccount(username: String): Result<Unit> {
        return userRepository.deleteAccount(username)
    }

    suspend fun logout(): Result<Unit> {
        return userRepository.logout()
    }

}

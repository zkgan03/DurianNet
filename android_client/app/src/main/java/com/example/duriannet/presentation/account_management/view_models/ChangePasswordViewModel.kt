package com.example.duriannet.presentation.account_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.presentation.account_management.state.ChangePasswordState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChangePasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _changePasswordState = MutableStateFlow(ChangePasswordState())
    val changePasswordState: StateFlow<ChangePasswordState> = _changePasswordState

    fun changePassword(currentPassword: String, newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            if (newPassword != confirmPassword) {
                _changePasswordState.value = ChangePasswordState(error = "Passwords do not match")
                return@launch
            }

            val result = userRepository.changePassword(currentPassword, newPassword)
            if (result.isSuccess) {
                _changePasswordState.value = ChangePasswordState(isPasswordChanged = true)
            } else {
                _changePasswordState.value = ChangePasswordState(error = result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}
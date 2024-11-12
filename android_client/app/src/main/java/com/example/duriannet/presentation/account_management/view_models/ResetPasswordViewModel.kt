package com.example.duriannet.presentation.account_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.presentation.account_management.state.ResetPasswordState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _resetPasswordState = MutableStateFlow(ResetPasswordState())
    val resetPasswordState: StateFlow<ResetPasswordState> = _resetPasswordState

    fun resetPassword(newPassword: String, confirmPassword: String) {
        viewModelScope.launch {
            if (newPassword != confirmPassword) {
                _resetPasswordState.value = ResetPasswordState(error = "Passwords do not match")
                return@launch
            }

            val result = userRepository.resetPassword(newPassword)
            if (result.isSuccess) {
                _resetPasswordState.value = ResetPasswordState(isPasswordReset = true)
            } else {
                _resetPasswordState.value = ResetPasswordState(error = result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}
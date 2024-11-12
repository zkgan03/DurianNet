package com.example.duriannet.presentation.account_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.presentation.account_management.state.ForgetPasswordState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgetPasswordViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _forgetPasswordState = MutableStateFlow(ForgetPasswordState())
    val forgetPasswordState: StateFlow<ForgetPasswordState> = _forgetPasswordState

    fun sendResetEmail(email: String) {
        viewModelScope.launch {
            val result = userRepository.sendResetEmail(email)
            if (result.isSuccess) {
                _forgetPasswordState.value = ForgetPasswordState(isEmailSent = true)
            } else {
                _forgetPasswordState.value = ForgetPasswordState(error = result.exceptionOrNull()?.message ?: "Unknown error")
            }
        }
    }
}
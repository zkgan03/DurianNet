package com.example.duriannet.presentation.account_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.presentation.account_management.state.OTPValidationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OTPViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _otpValidationState = MutableStateFlow<OTPValidationState>(OTPValidationState.Idle)
    val otpValidationState: StateFlow<OTPValidationState> = _otpValidationState

    fun validateOTP(email: String, otp: String) {
        viewModelScope.launch {
            _otpValidationState.value = OTPValidationState.Loading
            try {
                val result = userRepository.validateOTP(email, otp)
                if (result.isSuccess) {
                    _otpValidationState.value = OTPValidationState.Success
                } else {
                    val errorMessage = result.exceptionOrNull()?.message ?: "Invalid OTP or OTP has expired"
                    _otpValidationState.value = OTPValidationState.Error(errorMessage)
                }
            } catch (e: Exception) {
                _otpValidationState.value = OTPValidationState.Error(e.message ?: "An unexpected error occurred")
            }
        }
    }
}


/*
package com.example.duriannet.presentation.account_management.view_models

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.presentation.account_management.state.OTPValidationState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OTPViewModel @Inject constructor(
    private val userRepository: UserRepository
) : ViewModel() {

    private val _otpValidationState = MutableStateFlow<OTPValidationState>(OTPValidationState.Idle)
    val otpValidationState: StateFlow<OTPValidationState> = _otpValidationState

    fun validateOTP(email: String, otp: String) {
        viewModelScope.launch {
            _otpValidationState.value = OTPValidationState.Loading
            val result = userRepository.validateOTP(email, otp)
            if (result.isSuccess) {
                _otpValidationState.value = OTPValidationState.Success
            } else {
                _otpValidationState.value = OTPValidationState.Error(result.exceptionOrNull()?.message ?: "Unknown error occurred")
            }
        }
    }
}
*/

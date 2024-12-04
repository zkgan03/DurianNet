package com.example.duriannet.presentation.account_management.state

sealed class OTPValidationState {
    object Idle : OTPValidationState()
    object Loading : OTPValidationState()
    object Success : OTPValidationState()
    data class Error(val message: String) : OTPValidationState()
}

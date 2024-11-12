package com.example.duriannet.presentation.account_management.state

data class ResetPasswordState(
    val isPasswordReset: Boolean = false,
    val error: String = ""
)
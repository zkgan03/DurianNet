package com.example.duriannet.presentation.account_management.state

data class ForgetPasswordState(
    val isEmailSent: Boolean = false,
    val error: String = ""
)
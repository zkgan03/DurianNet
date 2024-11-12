package com.example.duriannet.presentation.account_management.state

data class ChangePasswordState(
    val isPasswordChanged: Boolean = false,
    val error: String = ""
)
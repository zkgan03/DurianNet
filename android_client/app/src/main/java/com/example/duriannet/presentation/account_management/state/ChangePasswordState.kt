package com.example.duriannet.presentation.account_management.state

data class ChangePasswordState(
    val loading: Boolean = false,
    val isPasswordChanged: Boolean = false,
    val error: String = ""
)

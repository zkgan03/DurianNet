package com.example.duriannet.presentation.account_management.state

data class LoginState(
    val isLoggedIn: Boolean = false,
    val username: String = "",
    val token: String = "",
    val error: String = ""
)

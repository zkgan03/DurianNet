package com.example.duriannet.presentation.account_management.state

data class EditProfileState(
    val username: String = "",
    val fullname: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val isProfileUpdated: Boolean = false,
    val error: String = ""
)
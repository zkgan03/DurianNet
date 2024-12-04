package com.example.duriannet.presentation.account_management.state

data class EditProfileState(
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profilePicture: String? = null,
    val isProfileUpdated: Boolean = false,
    val error: String = ""
)

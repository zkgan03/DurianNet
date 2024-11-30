package com.example.duriannet.presentation.account_management.state

data class ProfileState(
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val favoriteDurians: List<String> = emptyList(),
    val error: String = ""
)

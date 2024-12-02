package com.example.duriannet.presentation.account_management.state

data class ProfileState(
    val username: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val profileImageUrl: String = "",
    val favoriteDurians: List<Pair<Int, String>> = emptyList(),
    val loading: Boolean = false,
    val error: String = ""
)

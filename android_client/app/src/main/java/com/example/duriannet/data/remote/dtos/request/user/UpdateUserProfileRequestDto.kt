package com.example.duriannet.data.remote.dtos.request.user

data class UpdateUserProfileRequestDto(
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val profilePicture: String?
)

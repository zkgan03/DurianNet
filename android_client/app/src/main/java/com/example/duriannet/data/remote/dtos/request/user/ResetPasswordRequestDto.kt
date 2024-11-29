package com.example.duriannet.data.remote.dtos.request.user

data class ResetPasswordRequestDto(
    val email: String,
    val newPassword: String
)

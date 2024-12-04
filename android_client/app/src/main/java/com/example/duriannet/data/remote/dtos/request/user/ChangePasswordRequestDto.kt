package com.example.duriannet.data.remote.dtos.request.user

data class ChangePasswordRequestDto(
    val currentPassword: String,
    val password: String
)

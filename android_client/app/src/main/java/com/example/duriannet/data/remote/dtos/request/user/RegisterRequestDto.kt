package com.example.duriannet.data.remote.dtos.request.user

data class RegisterRequestDto(
    val username: String,
    val email: String,
    val password: String
)

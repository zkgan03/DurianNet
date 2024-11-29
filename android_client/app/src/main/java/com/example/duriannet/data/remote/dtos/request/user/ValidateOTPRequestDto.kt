package com.example.duriannet.data.remote.dtos.request.user

data class ValidateOTPRequestDto(
    val email: String,
    val otp: String
)


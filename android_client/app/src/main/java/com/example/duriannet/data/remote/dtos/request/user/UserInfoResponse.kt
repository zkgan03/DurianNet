package com.example.duriannet.data.remote.dtos.request.user

data class UserInfoResponse(
    val userId: String,   // User ID
    val email: String,  // User Email
    val jit: String,   // JWT ID
    val exp: String,   // Expiration Time
)

package com.example.duriannet.data.remote.dtos.response

data class NewUserDto(
    val username: String,
    val email: String,
    val token: String
)

/*data class UserDetailsResponseDto(
    val username: String,
    val fullName: String,
    val email: String,
    val phoneNumber: String,
    val profilePicture: String
)*/

data class UserProfileDto(
    val userName: String,      // Matches "userName" in the JSON
    val email: String,         // Matches "email" in the JSON
    val fullName: String,      // Matches "fullName" in the JSON
    val phoneNumber: String,   // Matches "phoneNumber" in the JSON
    val profilePicture: String? // Matches "profilePicture" in the JSON, can be nullable
)
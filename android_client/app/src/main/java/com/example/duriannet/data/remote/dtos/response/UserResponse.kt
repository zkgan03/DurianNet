package com.example.duriannet.data.remote.dtos.response

import com.example.duriannet.models.User
import com.example.duriannet.models.UserStatus
import com.example.duriannet.models.UserType

/*data class NewUserDto(
    val username: String,
    val email: String,
    val token: String
)*/

data class NewUserDto(
    val username: String,
    val email: String,
    val accessToken: String,
    //val refreshToken: String
)

data class UserProfileDto(
    val userName: String,      // Matches "userName" in the JSON
    val email: String,         // Matches "email" in the JSON
    val fullName: String,      // Matches "fullName" in the JSON
    val phoneNumber: String,   // Matches "phoneNumber" in the JSON
    val profilePicture: String? // Matches "profilePicture" in the JSON, can be nullable
)

fun UserProfileDto.toUser() : User = User(
    id = "",
    username = userName,
    fullName = fullName,
    email = email,
    phoneNumber = phoneNumber,
    profilePicture = profilePicture,
    userType = UserType.User,
    userStatus = UserStatus.Active
)

data class UserDetailsDto(
    val id: String,
    val fullName: String?,
    val email: String?,
    val username: String,
    val phoneNumber: String?,
    val password: String?,
    val userType: String,
    val status: String,
    val profilePicture: String?
)
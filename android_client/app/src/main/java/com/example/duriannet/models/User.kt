package com.example.duriannet.models

data class User(
    val id: String,
    val username: String,
    val fullName: String?,
    val email: String,
    val phoneNumber: String?,
    val profilePicture: String?,
    val userType: UserType,
    val userStatus: UserStatus
)

enum class UserType {
    User, Admin, SuperAdmin
}

enum class UserStatus {
    Active, Deleted, Suspended
}
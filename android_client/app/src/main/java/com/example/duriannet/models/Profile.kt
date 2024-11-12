package com.example.duriannet.models

data class Profile(
    val username: String,
    val fullname: String,
    val email: String,
    val phoneNumber: String,
    val favoriteDurians: List<String>
)
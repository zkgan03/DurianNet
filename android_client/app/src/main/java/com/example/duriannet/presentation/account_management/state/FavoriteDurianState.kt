package com.example.duriannet.presentation.account_management.state

data class FavoriteDurianState(
    val favoriteDurians: List<String> = emptyList(),
    val error: String = ""
)
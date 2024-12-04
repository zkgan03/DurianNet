package com.example.duriannet.presentation.account_management.state

import com.example.duriannet.data.remote.dtos.response.DurianProfileForUserResponseDto

data class FavoriteDurianState(
    val allDurians: List<DurianProfileForUserResponseDto> = emptyList(),
    val filteredDurians: List<DurianProfileForUserResponseDto> = emptyList(),
    val favoriteDurianIds: List<Int> = emptyList(),
    val error: String = ""
)

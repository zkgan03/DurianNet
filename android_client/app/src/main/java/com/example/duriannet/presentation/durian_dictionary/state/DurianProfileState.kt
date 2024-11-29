package com.example.duriannet.presentation.durian_dictionary.state

import com.example.duriannet.data.remote.dtos.response.DurianProfileForUserResponseDto

data class DurianProfileState(
    val allDurians: List<DurianProfileForUserResponseDto> = emptyList(),
    val filteredDurians: List<DurianProfileForUserResponseDto> = emptyList(),
    val error: String = ""
)


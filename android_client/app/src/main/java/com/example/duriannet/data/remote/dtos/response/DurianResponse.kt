package com.example.duriannet.data.remote.dtos.response

data class DurianListResponseDto(
    val durianId: Int,
    val durianName: String,
    val durianImage: String
)

data class DurianProfileResponseDto(
    val durianId: Int,
    val durianName: String,
    val durianCode: String,
    val durianDescription: String,
    val characteristics: String,
    val tasteProfile: String,
    val durianImage: String,
    val durianVideoUrl: String,
    val durianVideoDescription: String
)

data class DurianProfileForUserResponseDto(
    val durianId: Int,
    val durianName: String,
    val durianImage: String
)

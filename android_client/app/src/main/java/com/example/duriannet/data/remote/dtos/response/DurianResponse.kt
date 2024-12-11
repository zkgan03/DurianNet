package com.example.duriannet.data.remote.dtos.response

import com.google.gson.annotations.SerializedName

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
    @SerializedName("videoUrl") val durianVideoUrl: String?,
    @SerializedName("description") val durianVideoDescription: String
)



data class DurianProfileForUserResponseDto(
    val durianId: Int,
    val durianName: String,
    val durianImage: String
)

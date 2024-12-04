package com.example.duriannet.models

data class DurianProfile(
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

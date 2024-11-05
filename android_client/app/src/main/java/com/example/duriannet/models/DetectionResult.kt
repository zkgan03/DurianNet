package com.example.duriannet.models

data class DetectionResult(
    val label: String,
    val confidence: Double,
    val top: Int,
    val left: Int,
    val width: Int,
    val height: Int,
)

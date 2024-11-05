package com.example.duriannet.services.detector.hub_dto

import com.example.duriannet.models.DetectionResult
import com.fasterxml.jackson.annotation.JsonProperty


data class DetectionResultDto(
    @JsonProperty("Label") val label: String,
    @JsonProperty("Confidence") val confidence: Double,
    @JsonProperty("Top") val top: Int,
    @JsonProperty("Left") val left: Int,
    @JsonProperty("Width") val width: Int,
    @JsonProperty("Height") val height: Int,
)

fun DetectionResultDto.toDetectionResult() = DetectionResult(
    label = label,
    confidence = confidence,
    top = top,
    left = left,
    width = width,
    height = height,
)
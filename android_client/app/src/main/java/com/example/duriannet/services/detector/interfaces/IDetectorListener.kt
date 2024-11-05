package com.example.duriannet.services.detector.interfaces

import android.graphics.Bitmap
import com.example.duriannet.models.DetectionResult

interface IDetectorListener {
    fun onInitialized()
    fun onStopped()
    fun onError(error: String)
    fun onDetect(
        results: Array<DetectionResult>,
        inferenceTime: Long,
        detectWidth: Int,
        detectHeight: Int,
        inputImage: Bitmap,
    )

    fun onEmptyDetect()
}
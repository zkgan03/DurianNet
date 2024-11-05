package com.example.duriannet.services.detector.interfaces

import android.graphics.Bitmap
import com.example.duriannet.models.DetectionResult
import com.example.duriannet.services.detector.base.DetectorConfiguration
import com.example.duriannet.services.detector.enum.DetectorStatusEnum

interface IDetector {

    fun start()
    fun stop()
    fun detectLiveStream(bitmap: Bitmap)
    fun detectImage(bytes: ByteArray): Triple<Array<DetectionResult>, Int, Int>
    fun detectImage(bitmap: Bitmap): Triple<Array<DetectionResult>, Int, Int>

    fun updateListener(detectorListener: IDetectorListener?)
    fun updateConfigurations(config: DetectorConfiguration)

    fun status(): DetectorStatusEnum
}


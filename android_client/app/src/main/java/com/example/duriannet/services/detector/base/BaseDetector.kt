package com.example.duriannet.services.detector.base

import android.graphics.Bitmap
import com.example.duriannet.models.DetectionResult
import com.example.duriannet.services.detector.enum.DetectorStatusEnum
import com.example.duriannet.services.detector.interfaces.IDetector
import com.example.duriannet.services.detector.interfaces.IDetectorListener

abstract class BaseDetector(
    protected val config: DetectorConfiguration = DetectorConfiguration(),
    protected var detectorListener: IDetectorListener?,
) : IDetector {

    protected var currentStatus: DetectorStatusEnum = DetectorStatusEnum.INITIALIZING

    protected var tensorWidth = 0
    protected var tensorHeight = 0
    protected var numChannel = 0
    protected var predictions = 0

    override fun status(): DetectorStatusEnum {
        return currentStatus
    }

    override fun start() {
        TODO("Not yet implemented")
    }

    override fun stop() {
        TODO("Not yet implemented")
    }

    override fun detectLiveStream(bitmap: Bitmap) {
        TODO("Not yet implemented")
    }

    override fun detectImage(bytes: ByteArray): Triple<Array<DetectionResult>, Int, Int> {
        TODO("Not yet implemented")
    }

    override fun detectImage(bitmap: Bitmap): Triple<Array<DetectionResult>, Int, Int> {
        TODO("Not yet implemented")
    }

    override fun updateListener(detectorListener: IDetectorListener?) {
        this.detectorListener = detectorListener
    }

    override fun updateConfigurations(config: DetectorConfiguration) {
        processIfInitialized {
            this.config.cnfThreshold = config.cnfThreshold
            this.config.iouThreshold = config.iouThreshold
            this.config.maxNumberDetection = config.maxNumberDetection
            this.config.currentDelegate = config.currentDelegate
        }
    }

    protected inline fun processIfInitialized(block: () -> Unit) {
        if (currentStatus != DetectorStatusEnum.INITIALIZED) {
            detectorListener?.onError("Detector not initialized")
            return
        }

        block()
    }

}
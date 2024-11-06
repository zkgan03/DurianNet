package com.example.duriannet.presentation.detector.view_models

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.services.detector.interfaces.IDetector
import com.example.duriannet.services.detector.DetectionHub
import com.example.duriannet.services.detector.YoloDetector
import com.example.duriannet.services.detector.base.DetectorConfiguration
import com.example.duriannet.services.detector.enum.DetectorStatusEnum
import com.example.duriannet.services.detector.interfaces.IDetectorListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetectorViewModel : ViewModel() {

    private var _detector: IDetector? = null
    val detector: IDetector?
        get() = _detector

    private var _isServerDetection = false

    private val detectorConfiguration: DetectorConfiguration = DetectorConfiguration()

    fun updateListener(detectorListener: IDetectorListener) {
        _detector?.updateListener(detectorListener)
    }

    val isDetectorInitialized: Boolean
        get() = _detector != null && _detector?.status() == DetectorStatusEnum.INITIALIZED


    fun setConfidenceThreshold(threshold: Float) {
        detectorConfiguration.cnfThreshold = threshold
        _detector?.updateConfigurations(detectorConfiguration) // although pass by reference, but still need to call updateConfigurations to run the necessary process
    }

    fun setIouThreshold(threshold: Float) {
        detectorConfiguration.iouThreshold = threshold
        _detector?.updateConfigurations(detectorConfiguration)
    }

    fun setMaxNumberDetection(maxNumberDetection: Int) {
        detectorConfiguration.maxNumberDetection = maxNumberDetection
        _detector?.updateConfigurations(detectorConfiguration)
    }

    fun setDetectionModel(
        isServerDetection: Boolean,
        detectorListener: IDetectorListener,
        context: Context,
    ) {
        if (_isServerDetection == isServerDetection) return

        stopDetector()
        startDetector(isServerDetection, detectorListener, context)
    }

    fun startDetector(
        isServerDetection: Boolean,
        detectorListener: IDetectorListener,
        context: Context,
    ) {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {

                if (_detector != null) return@withContext

                _isServerDetection = isServerDetection

                _detector = if (isServerDetection) {
                    DetectionHub(
                        detectorListener = detectorListener,
                        config = detectorConfiguration,
                    )
                } else {
                    YoloDetector(
                        detectorListener = detectorListener,
                        config = detectorConfiguration,
                        context = context
                    )
                }
            }
        }
    }

    fun stopDetector() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _detector?.stop()
                _detector = null
            }
        }

    }
}
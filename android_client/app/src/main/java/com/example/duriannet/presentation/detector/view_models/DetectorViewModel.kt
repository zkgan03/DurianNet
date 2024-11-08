package com.example.duriannet.presentation.detector.view_models

import android.content.Context
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.duriannet.services.detector.interfaces.IDetector
import com.example.duriannet.services.detector.DetectionHub
import com.example.duriannet.services.detector.YoloDetector
import com.example.duriannet.services.detector.base.DetectorConfiguration
import com.example.duriannet.services.detector.enum.DetectorStatusEnum
import com.example.duriannet.services.detector.interfaces.IDetectorListener
import com.example.duriannet.utils.Event
import com.example.duriannet.utils.EventBus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class DetectorViewModel : ViewModel() {

    private var _detector: IDetector? = null
    val detector: IDetector?
        get() = _detector

    private var _isServerDetection = false
    private var _isFirstInit = true
    val isFirstInit: Boolean
        get() = _isFirstInit

    private val detectorConfiguration: DetectorConfiguration = DetectorConfiguration()

    fun getDetectionSize(): Pair<Int, Int> {
        return if (_detector is DetectionHub) {
            Pair(DetectionHub.DETECT_IMG_SIZE, DetectionHub.DETECT_IMG_SIZE)
        } else {
            Pair(YoloDetector.DETECT_IMG_SIZE, YoloDetector.DETECT_IMG_SIZE)
        }
    }

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

        viewModelScope.launch(Dispatchers.IO) {
            if (isDetectorInitialized) return@launch

            Log.e("detector view model", "start detector")

            _isServerDetection = isServerDetection

            _detector = if (isServerDetection) {
                try {
                    DetectionHub(
                        detectorListener = detectorListener,
                        config = detectorConfiguration,
                    )
                } catch (e: Exception) {

                    Log.e("DetectorViewModel", "Error starting DetectionHub: $e")

                    EventBus.sendEvent(Event.Toast("Error starting DetectionHub: $e, now using YoloDetector"))

                    null
                }
            } else {
                YoloDetector(
                    detectorListener = detectorListener,
                    config = detectorConfiguration,
                    context = context
                )

            }
        }
    }

    fun stopDetector() {
        viewModelScope.launch(Dispatchers.IO) {
            _detector?.stop()
            _detector = null

        }

    }
}
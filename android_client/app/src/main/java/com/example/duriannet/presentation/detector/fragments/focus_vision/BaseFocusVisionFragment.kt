package com.example.duriannet.presentation.detector.fragments.focus_vision

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.navGraphViewModels
import com.example.duriannet.R
import com.example.duriannet.models.DetectionResult
import com.example.duriannet.presentation.detector.view_models.DetectorViewModel
import com.example.duriannet.services.detector.DetectionHub
import com.example.duriannet.services.detector.YoloDetector
import com.example.duriannet.services.detector.base.DetectorConfiguration
import com.example.duriannet.services.detector.interfaces.IDetector
import com.example.duriannet.services.detector.interfaces.IDetectorListener
import com.example.duriannet.services.detector.utils.Common
import drawResults
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

abstract class BaseFocusVisionFragment : Fragment(), IDetectorListener {

    private var _isProcessing: Boolean = false
    val isProcessing get() = _isProcessing

    private val detectionResultBundle: MutableList<Array<DetectionResult>> = mutableListOf()
    private val numberOfDetection: Int = 20

    protected val detectorViewModel: DetectorViewModel by viewModels()

    private var finalResults: Array<DetectionResult> = emptyArray()

    fun getCompletion(): Float {
        return detectionResultBundle.size.toFloat() / numberOfDetection.toFloat()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        detectorViewModel.apply {
            // set default values
            setConfidenceThreshold(resources.getString(R.string.default_confidence_threshold).toFloat())
            setIouThreshold(resources.getString(R.string.default_iou_threshold).toFloat())
            setMaxNumberDetection(resources.getString(R.string.default_max_number_detection).toInt())
        }
    }

    override fun onResume() {
        super.onResume()
        detectorViewModel.updateListener(this)
        detectionResultBundle.clear()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        detectorViewModel.stopDetector()
    }

    fun stopProcessing() {
        _isProcessing = false
        detectionResultBundle.clear()
    }

    fun startProcessing() {
        _isProcessing = true
    }

    override fun onInitialized() {

    }

    override fun onStopped() {

    }

    override fun onError(error: String) {

    }

    override fun onDetect(
        results: Array<DetectionResult>,
        inferenceTime: Long,
        detectWidth: Int,
        detectHeight: Int,
        inputImage: Bitmap,
    ) {
        activity?.runOnUiThread {
            if (!isAdded || !_isProcessing) {
                return@runOnUiThread
            }
            detectionResultBundle.add(results)
            if (detectionResultBundle.size >= numberOfDetection) {
                _isProcessing = false
                processDetectionResultsBundle(inputImage)
            }
        }
    }

    abstract fun onCompleteDetect(finalResults: Array<DetectionResult>, imageResult: Bitmap)

    private fun processDetectionResultsBundle(
        inputImg: Bitmap,
        iouThreshold: Float = 0.5f,
        detectNumberThreshold: Int = 15,
    ) {
        /**
         * 1. Iterate through the detection result bundle
         * 2. Iterate through each frame results
         * 3. Compare current frame result with the tracked detection results by calculating the IoU
         * 4. If the IoU is greater than the threshold, assume the bounding box is the same as previous frame
         *    and add it to the tracked detection results
         * */
        val trackedDetectionResults: MutableList<MutableList<DetectionResult>> = mutableListOf()

        Log.e("DetectionViewModel", "detectionResultBundle: ${detectionResultBundle.size}")
        // Iterate through the detection result bundle
        for (frameResults in detectionResultBundle) {

            Log.e("DetectionViewModel", "========================================================")
            Log.e("DetectionViewModel", "frameResults: ${frameResults.size}")
            for (result in frameResults) {

                //print all info of result
                Log.e(
                    "DetectionViewModel",
                    "result: ${result.label}, ${result.confidence}, ${result.top}, ${result.left}, ${result.width}, ${result.height}"
                )

                // Check if the bounding box is the same as the bounding box in the tracked detection results
                var isTracked = false
                for (trackedResults in trackedDetectionResults) {

                    val iou = Common.calculateIoU(result, trackedResults.first())
                    Log.e("DetectionViewModel", "iou: $iou")

                    if (Common.calculateIoU(result, trackedResults.first()) >= iouThreshold) {
                        trackedResults.add(result)
                        isTracked = true
                        break
                    }
                }

                // If the bounding box is not the same as the bounding box in the tracked detection results, add the bounding box into the tracked detection results
                if (!isTracked) {
                    trackedDetectionResults.add(mutableListOf(result))
                }

            }
        }

        // eliminate the bounding boxes that is less than detectNumberThreshold
        val filteredDetectionResults = trackedDetectionResults.filter { it.size >= detectNumberThreshold }

        Log.e("DetectionViewModel", "filteredDetectionResults: ${filteredDetectionResults.size}")
        // calculate the average of the bounding box probability
        val averagedDetectionResults = filteredDetectionResults.map { trackedResults ->
            DetectionResult(
                label = trackedResults.first().label,
                confidence = trackedResults.map { it.confidence }.average(),
                top = trackedResults.map { it.top }.average().toInt(),
                left = trackedResults.map { it.left }.average().toInt(),
                width = trackedResults.map { it.width }.average().toInt(),
                height = trackedResults.map { it.height }.average().toInt()
            )
        }

        finalResults = averagedDetectionResults.toTypedArray()

        val detectionSize = detectorViewModel.getDetectionSize()

        val imageResult =
            inputImg.drawResults(finalResults, detectionSize, borderColor = ContextCompat.getColor(requireContext(), R.color.accent_dark_green))

        onCompleteDetect(finalResults, imageResult)
    }


    override fun onEmptyDetect() {

    }


}
package com.example.duriannet.presentation.seller_locator.fragments

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentAddSellerDetectionBinding
import com.example.duriannet.models.DetectionResult
import com.example.duriannet.models.DurianType
import com.example.duriannet.presentation.seller_locator.view_models.AddSellerViewModel
import com.example.duriannet.services.common.AccelerometerSensor
import com.example.duriannet.services.common.CameraManager
import com.example.duriannet.services.detector.DetectionHub
import com.example.duriannet.services.detector.YoloDetector
import com.example.duriannet.services.detector.enum.DetectorStatusEnum
import com.example.duriannet.services.detector.interfaces.IDetector
import com.example.duriannet.services.detector.interfaces.IDetectorListener
import com.example.duriannet.services.detector.utils.Utils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@AndroidEntryPoint
class AddSellerDetectionFragment : Fragment(), IDetectorListener {

    private var _binding: FragmentAddSellerDetectionBinding? = null
    private val binding get() = _binding!!

    private var cameraManager: CameraManager? = null
    private lateinit var accelerometerSensor: AccelerometerSensor


    private val navController: NavController by lazy { findNavController() }

    private val viewModel: AddSellerViewModel by hiltNavGraphViewModels(R.id.add_seller_nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddSellerDetectionBinding.inflate(inflater, container, false)

        cameraManager = CameraManager(requireContext(), this, binding.viewFinder)
        accelerometerSensor = AccelerometerSensor(requireContext())

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setUpCameraManager()
        setupAccelerometerSensor()
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                setupDetector()
            }
        }
    }

    private fun setupAccelerometerSensor() {
        accelerometerSensor = AccelerometerSensor(requireContext()) // initialize accelerometer sensor

        accelerometerSensor.onShakeDetected = {
            binding.bottomPromptChip.text = "Please hold your device steady!"
            binding.processProgress.text = "0%"

            //make flip camera button and flash button visible
            binding.flipCameraButton.visibility = View.VISIBLE
            binding.toggleFlashButton.visibility = View.VISIBLE

            Log.e(TAG, "Shake detected")

            isProcessing = false

            // stop the detection when shake is detected
            detectionResultBundle.clear() // clear the detection result bundle
            cameraManager?.clearAnalyzer() // clear the image analyzer
            accelerometerSensor.stop() // stop the accelerometer sensor
            binding.overlay.clear() // clear the overlay
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        releaseCamera()
        _binding = null
    }

    private fun setupUI() {
        binding.flipCameraButton.setOnClickListener {
            cameraManager?.toggleCamera()
        }

        binding.toggleFlashButton.setOnClickListener {
            cameraManager?.toggleFlash()

            binding.toggleFlashButton.setImageResource(
                if (cameraManager?.getFlashMode() == CameraManager.FlashMode.ON) {
                    R.drawable.baseline_flash_on_24
                } else {
                    R.drawable.baseline_flash_off_24
                }
            )
        }

        binding.btnStartDetection.setOnClickListener {
            binding.bottomPromptChip.text = "Processing..."

            cameraManager?.enableGestures(false)

            //make flip camera button and flash button invisible
            binding.flipCameraButton.visibility = View.INVISIBLE
            binding.toggleFlashButton.visibility = View.INVISIBLE

            isProcessing = true
            cameraManager?.setAnalyzer()
            accelerometerSensor.start()
        }

        binding.bottomPromptChip.text = "Hold your device steady and press the button to start detection"
    }

    private fun setUpCameraManager() {
        cameraManager?.apply {

            setOnInitializedListener {
                activity?.runOnUiThread {
                    Log.e(TAG, "onInitialized")
                }

            }

            setOnImageAnalyzedListener { bitmap ->
                if (detector?.status() == DetectorStatusEnum.INITIALIZED) {
                    detector?.detectLiveStream(bitmap)
                }
            }

            setOnErrorListener { error ->
                activity?.runOnUiThread {
                    Toast.makeText(requireActivity(), "Error: $error", Toast.LENGTH_LONG).show()
                }
            }

            setOnFocusAndMeteringListener { event ->
                // draw focus circle
                val x = event.x
                val y = event.y
                val focusCircle = RectF(x - 50, y - 50, x + 50, y + 50)
                binding.focusCircleView.focusCircle = focusCircle
                binding.focusCircleView.invalidate()
            }

            startCamera()
        }
    }

    private fun releaseCamera() {
        cameraManager?.release()
        cameraManager = null
    }

    /**
     * Detector setup
     */

    private var detector: IDetector? = null
    private var isProcessing = false
    private val detectNumber = 20
    private val detectionResultBundle = mutableListOf<Array<DetectionResult>>()

    private suspend fun setupDetector() {
        withContext(Dispatchers.IO) {
            detector = YoloDetector(
                context = requireContext(),
                detectorListener = this@AddSellerDetectionFragment
            ) // TODO : change to server detection
            viewModel.detectionSize = Pair(DetectionHub.DETECT_IMG_SIZE, DetectionHub.DETECT_IMG_SIZE)
        }
    }

    override fun onInitialized() {
        // run on ui / main thread
        activity?.runOnUiThread {
            // show the camera view only after the connection is establish and the camera is ready
            binding.progress.visibility = View.GONE
        }
    }

    override fun onStopped() {
        activity?.runOnUiThread {
            cameraManager?.clearAnalyzer()
            binding.overlay.clear()
        }
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Log.e(TAG, "Error  : $error")
            Toast.makeText(requireActivity(), "Error: $error", Toast.LENGTH_LONG).show()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onDetect(
        results: Array<DetectionResult>,
        inferenceTime: Long,
        detectWidth: Int,
        detectHeight: Int,
        inputImage: Bitmap,
    ) {
        activity?.runOnUiThread {

            if (_binding == null || !isAdded || !isProcessing) {
                return@runOnUiThread
            }


//            Log.e(TAG, "on Detect number: ${viewModel.detectionResultBundle.size}")
            detectionResultBundle.add(results)

            binding.processProgress.text = "${(detectionResultBundle.size / detectNumber.toFloat() * 100).toInt()}%"

            binding.overlay.clear()
            binding.overlay.apply {
                setResults(
                    results,
                    Pair(detectHeight, detectWidth),
                    Pair(binding.viewFinder.height, binding.viewFinder.width),
                    showText = false
                )
            }

            if (detectionResultBundle.size >= detectNumber) {
                detectionCompleted(inputImage!!)
            }
        }
    }

    private fun detectionCompleted(inputImage: Bitmap) {
        isProcessing = false
        accelerometerSensor.stop()
        cameraManager?.release()

        Log.e(TAG, "onDetectionCompleted")
        Toast.makeText(requireActivity(), "Detection Completed", Toast.LENGTH_SHORT).show()

        processDetectionResultsBundle(inputImage)

        //TODO : check if the detection result has durian or not, if not, show the error message in prompt chip, then require the user to detect again
        //TODO : Set the input state : durian type to the view model
        viewModel.inputSellerDurianType(setOf(DurianType.D24, DurianType.MusangKing)) // add dummy durian type

        // navigate to the result fragment to show the detection result
        if (navController.currentDestination?.id == R.id.addSellerDetectionFragment)
            navController.navigate(R.id.action_addSellerDetectionFragment_to_addSellerFragment)
    }

    private fun processDetectionResultsBundle(
        inputImg: Bitmap,
        iouThreshold: Float = 0.5f,
        detectNumberThreshold: Int = 15,
    ) {
        viewModel.imageResult = inputImg
//        this.detectionResults = detectionResultBundle.last()

        // apply iou threshold to check the bounding box in the frames in detection result bundle is the same bounding box or not
        // if the bounding box is the same, keep counting and tracking
        // then calculate the average of the bounding box probability
        // if the number of bounding box is less than 15, remove the bounding box from the detection result bundle

        // track the similar bounding box in the detection result bundle
        // if the bounding box is the same, add into the same list
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

                    val iou = Utils.calculateIoU(result, trackedResults.first())
                    Log.e("DetectionViewModel", "iou: $iou")

                    if (Utils.calculateIoU(result, trackedResults.first()) >= iouThreshold) {
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

        viewModel.detectionResults = averagedDetectionResults.toTypedArray()
    }


    override fun onEmptyDetect() {
        activity?.runOnUiThread {
            Log.e(TAG, "onEmptyDetect")
            binding.bottomPromptChip.text = "No object detected!"
            binding.overlay.clear()
        }
    }


    companion object {
        const val TAG = "AddSellerDetectionFragment"
    }

}
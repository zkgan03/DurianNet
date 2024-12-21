package com.example.duriannet.presentation.seller_locator.fragments.add_seller

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentAddSellerDetectionBinding
import com.example.duriannet.models.DetectionResult
import com.example.duriannet.models.DurianType
import com.example.duriannet.presentation.detector.fragments.focus_vision.BaseFocusVisionFragment
import com.example.duriannet.presentation.seller_locator.view_models.AddSellerViewModel
import com.example.duriannet.services.common.AccelerometerSensor
import com.example.duriannet.services.common.CameraManager
import com.example.duriannet.services.common.GoogleMapManager
import com.example.duriannet.services.detector.DetectionHub
import com.example.duriannet.utils.Event
import com.example.duriannet.utils.EventBus
import dagger.hilt.android.AndroidEntryPoint
import drawResults
import kotlinx.coroutines.launch

@AndroidEntryPoint
class AddSellerDetectionFragment : BaseFocusVisionFragment() {

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

        detectorViewModel.startDetector(
            true,
            this,
            requireContext()
        )

        cameraManager = CameraManager(requireContext(), this, binding.viewFinder)
        accelerometerSensor = AccelerometerSensor(requireContext())

        // Inflate the layout for this fragment
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bottomPromptChip.text = "Initializing..."
        setupActionBar()
        setupUI()
        setupAccelerometerSensor()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        releaseCamera()
        _binding = null
    }

    private fun setupAccelerometerSensor() {
        accelerometerSensor = AccelerometerSensor(requireContext()) // initialize accelerometer sensor

        accelerometerSensor.onShakeDetected = {
            super.stopProcessing()

            binding.apply {
                bottomPromptChip.text = "Please hold your device steady!"
                processProgress.text = "0%"

                //make flip camera button and flash button visible
                flipCameraButton.visibility = View.VISIBLE
                toggleFlashButton.visibility = View.VISIBLE
            }

            Log.e(TAG, "Shake detected")

            cameraManager?.clearAnalyzer() // clear the image analyzer
            accelerometerSensor.stop() // stop the accelerometer sensor
            binding.overlay.clear() // clear the overlay
        }
    }


    private fun setupActionBar() {

        val parentActivity = requireActivity() as AppCompatActivity

        parentActivity.setSupportActionBar(binding.toolbar)

        parentActivity.supportActionBar?.apply {
            title = "Add New Seller"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

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
            super.startProcessing()

            binding.apply {
                bottomPromptChip.text = "Processing..."

                //make flip camera button and flash button invisible
                flipCameraButton.visibility = View.INVISIBLE
                toggleFlashButton.visibility = View.INVISIBLE
            }

            cameraManager?.apply {
                enableGestures(false)
                setAnalyzer()
            }

            accelerometerSensor.start()
        }
    }

    private fun setUpCameraManager() {
        cameraManager?.apply {

            setOnInitializedListener {
                activity?.runOnUiThread {
                    Log.e(TAG, "onInitialized")
                }

            }

            setOnImageAnalyzedListener { bitmap ->
                if (detectorViewModel.isDetectorInitialized) {
                    detectorViewModel.detector?.detectLiveStream(bitmap)
                }
            }

            setOnErrorListener { error ->
                viewLifecycleOwner.lifecycleScope.launch {
                    EventBus.sendEvent(Event.Toast(error))
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


    override fun onCompleteDetect(finalResults: Array<DetectionResult>, imageResult: Bitmap) {
        accelerometerSensor.stop()
        cameraManager?.release()

        viewModel.drawnImageResult = imageResult.drawResults(
            finalResults,
            Pair(DetectionHub.DETECT_IMG_SIZE, DetectionHub.DETECT_IMG_SIZE)
        )

        viewModel.inputSellerDurianType(
            finalResults.mapNotNull {
                when (it.label) {
                    "d197" -> DurianType.MusangKing
                    "d24" -> DurianType.D24
                    "d200" -> DurianType.BlackThorn
                    else -> null
                }
            }.toHashSet()
        )

        GoogleMapManager.getUserLocation(requireContext()) { location ->
            viewModel.inputSellerLocation(location.latitude, location.longitude)
        }

        if (navController.currentDestination?.id == R.id.addSellerDetectionFragment)
            navController.navigate(R.id.action_addSellerDetectionFragment_to_addSellerFragment)
    }

    /**
     * Listener
     */

    override fun onInitialized() {
        // run on ui / main thread
        activity?.runOnUiThread {
            // show the camera view only after the connection is establish and the camera is ready
            binding.progress.visibility = View.GONE
            binding.bottomPromptChip.text = "Connecting to camera..."
            setUpCameraManager()
            binding.btnStartDetection.isEnabled = true
            binding.bottomPromptChip.text = "Hold your device steady and press the button to start detection"
        }
    }

    override fun onStopped() {
        activity?.runOnUiThread {
            if (_binding == null || !isAdded) return@runOnUiThread

            cameraManager?.clearAnalyzer()
            binding.overlay.clear()
        }
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            if (_binding == null || !isAdded) return@runOnUiThread

            Log.e(TAG, "Error  : $error")
            binding.bottomPromptChip.text = "Error to start detection"
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
        super.onDetect(results, inferenceTime, detectWidth, detectHeight, inputImage)

        activity?.runOnUiThread {
            if (_binding == null || !isAdded || !isProcessing) {
                return@runOnUiThread
            }

            binding.processProgress.text = "${(super.getCompletion() * 100).toInt()}%"
            binding.overlay.clear()
            binding.overlay.apply {
                setResults(
                    results,
                    Pair(detectHeight, detectWidth),
                    Pair(binding.viewFinder.height, binding.viewFinder.width),
                    showText = false
                )
            }
        }
    }

    override fun onEmptyDetect() {
        activity?.runOnUiThread {
            if (_binding == null || !isAdded || !isProcessing) {
                return@runOnUiThread
            }

            Log.e(TAG, "onEmptyDetect")
            binding.bottomPromptChip.text = "No object detected!"
            binding.overlay.clear()
        }
    }


    companion object {
        const val TAG = "AddSellerDetectionFragment"
    }

}
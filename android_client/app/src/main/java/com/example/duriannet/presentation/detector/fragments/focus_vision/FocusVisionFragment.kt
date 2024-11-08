package com.example.duriannet.presentation.detector.fragments.focus_vision

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.lifecycle.Lifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.duriannet.R
import com.example.duriannet.databinding.BottomSheetDetectorSettingsBinding
import com.example.duriannet.databinding.FragmentFocusVisionBinding
import com.example.duriannet.models.DetectionResult
import com.example.duriannet.presentation.detector.view_models.FocusVisionViewModel
import com.example.duriannet.presentation.seller_locator.fragments.add_seller.AddSellerDetectionFragment.Companion.TAG
import com.example.duriannet.services.common.AccelerometerSensor
import com.example.duriannet.services.common.CameraManager
import com.example.duriannet.services.detector.enum.DetectorStatusEnum
import com.google.android.material.bottomsheet.BottomSheetDialog


class FocusVisionFragment : BaseFocusVisionFragment() {

    private var _binding: FragmentFocusVisionBinding? = null
    private val binding get() = _binding!!

    private var cameraManager: CameraManager? = null
    private lateinit var accelerometerSensor: AccelerometerSensor

    private val navController: NavController by lazy { findNavController() }

    private val focusVisionViewModel: FocusVisionViewModel by navGraphViewModels(R.id.detector_nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFocusVisionBinding.inflate(inflater, container, false)

        detectorViewModel.startDetector(
            false,
            this,
            requireContext()
        )
        accelerometerSensor = AccelerometerSensor(requireContext()) // initialize accelerometer sensor
        cameraManager = CameraManager(requireContext(), this, binding.viewFinder)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.bottomPromptChip.text = "Initializing..."
        setupActionBar()
        setupUI()
        setupBottomSheetDialog()
        setupAccelerometerSensor()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        releaseCamera()
    }

    private fun releaseCamera() {
        cameraManager?.release()
        cameraManager = null
    }

    private fun setupActionBar() {
        val parentActivity = requireActivity() as AppCompatActivity

        parentActivity.setSupportActionBar(binding.toolbar)

        parentActivity.supportActionBar?.apply {
            title = "Focus Vision"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }


        parentActivity.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.detector_menu, menu)
                    menu.findItem(R.id.action_settings).isVisible = true
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_settings -> {
                            bottomSheetSetting.show()
                            true
                        }

                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )
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

    override fun onCompleteDetect(finalResults: Array<DetectionResult>, imageResult: Bitmap) {
        accelerometerSensor.stop()
        cameraManager?.release()

        focusVisionViewModel.setImageResult(imageResult)

        if (navController.currentDestination?.id == R.id.focusVisionFragment)
            navController.navigate(R.id.action_focusVisionFragment_to_focusVisionResultFragment)
    }


    // Bottom Sheet Settings
    private lateinit var bottomSheetSetting: BottomSheetDialog
    private lateinit var bottomSheetBinding: BottomSheetDetectorSettingsBinding
    private fun setupBottomSheetDialog() {
        bottomSheetSetting = BottomSheetDialog(requireContext())

        val bottomSheetView = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_detector_settings, null)
            .also { bottomSheetSetting.setContentView(it) }

        bottomSheetBinding = BottomSheetDetectorSettingsBinding.bind(bottomSheetView)

        // set up model selection dropdown list
        val modelAvailable = resources.getStringArray(R.array.detection_models)
        val adapter = ArrayAdapter(requireContext(), R.layout.dropdown_model_list, modelAvailable)
        bottomSheetBinding.actvModelSelection.setAdapter(adapter)
        bottomSheetBinding.actvModelSelection.setText(modelAvailable[0], false)


        bottomSheetBinding.apply {
            actvModelSelection.setOnItemClickListener { _, _, position, id ->
                when (position) {
                    0 -> {
                        detectorViewModel.setDetectionModel(
                            false,
                            this@FocusVisionFragment,
                            requireContext()
                        )
                    }

                    1 -> {
                        detectorViewModel.setDetectionModel(
                            true,
                            this@FocusVisionFragment,
                            requireContext()
                        )
                    }
                }
            }

            sliderMaxNumOfDetection.addOnChangeListener { _, value, _ ->
                tvMaxNumOfDetection.text = value.toInt().toString()
                detectorViewModel.setMaxNumberDetection(value.toInt())
            }

            sliderConfidenceThreshold.addOnChangeListener { _, value, _ ->
                tvConfidenceThreshold.text = String.format("%.2f", value)
                detectorViewModel.setConfidenceThreshold(value)
            }

            sliderIoUThreshold.addOnChangeListener { _, value, _ ->
                tvIoUThreshold.text = String.format("%.2f", value)
                detectorViewModel.setIouThreshold(value)
            }
        }
    }


    //listener

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
        super.onEmptyDetect()
        activity?.runOnUiThread {
            if (_binding == null || !isAdded) return@runOnUiThread

            binding.bottomPromptChip.text = "No object detected!"
            binding.overlay.clear()
        }
    }

    companion object {
        private const val TAG = "FocusVisionFragment"
    }

}
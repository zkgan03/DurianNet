package com.example.duriannet.presentation.detector.fragments.instant_detect

import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentInstantCameraBinding
import com.example.duriannet.models.DetectionResult
import com.example.duriannet.presentation.detector.fragments.instant_detect.BaseInstantDetectFragment.Companion
import com.example.duriannet.services.common.CameraManager


class InstantCameraFragment : BaseInstantDetectFragment() {

    private var _cameraBinding: FragmentInstantCameraBinding? = null
    private val cameraBinding get() = _cameraBinding!!

    private var cameraManager: CameraManager? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Log.e(TAG, "onCreateView")
        _cameraBinding = FragmentInstantCameraBinding.inflate(inflater, container, false)
        cameraManager = CameraManager(requireContext(), this, cameraBinding.viewFinder)

        return cameraBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupCameraManager()
    }

    private fun setupUI() {
        cameraBinding.flipCameraButton.setOnClickListener {
            cameraManager?.toggleCamera()
            cameraManager?.setAnalyzer()
        }

        cameraBinding.toggleFlashButton.setOnClickListener {
            cameraManager?.toggleFlash()

            cameraBinding.toggleFlashButton.setImageResource(
                if (cameraManager?.getFlashMode() == CameraManager.FlashMode.ON) {
                    R.drawable.baseline_flash_on_24
                } else {
                    R.drawable.baseline_flash_off_24
                }
            )
        }

    }

    private fun setupCameraManager() {
        cameraManager?.apply {

            setOnInitializedListener {
                activity?.runOnUiThread {
                    Log.e(TAG, "Camera onInitialized")
                }

            }

            setOnImageAnalyzedListener { bitmap ->

                if (viewModel.isDetectorInitialized) {
                    viewModel.detector?.detectLiveStream(bitmap)
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
                cameraBinding.focusCircleView.focusCircle = focusCircle
                cameraBinding.focusCircleView.invalidate()
            }

            startCamera()
        }
    }

    private fun releaseCameraManager() {
        cameraManager?.release()
        cameraManager = null
    }


    override fun onResume() {
        super.onResume()
        if (cameraManager == null) {
            setupCameraManager()
        }
    }


    //in viewpager, onPause will be called when we swipe to another fragment
    override fun onPause() {
        super.onPause()
        Log.e(TAG, "onPause")
    }


    override fun onDestroyView() {
        super.onDestroyView()
        releaseCameraManager()

        _cameraBinding = null;
        Log.e(TAG, "onDestroyView")

    }


    //
    // Listener Method
    //
    override fun onInitialized() {
        super.onInitialized()
        // run on ui / main thread
        activity?.runOnUiThread {
            Log.e(TAG, "onInitialized")
//            cameraExecutor = Executors.newSingleThreadExecutor()

            // Wait for the views to be properly laid out
            cameraBinding.viewFinder.post {
                // Set up the camera and its use cases
                cameraManager?.setAnalyzer()
            }

            // show the camera view only after the connection is establish and the camera is ready
            cameraBinding.progress.visibility = View.GONE
        }
    }

    override fun onStopped() {
        super.onStopped()
        activity?.runOnUiThread {
            Log.e(TAG, "onStopped")

            if (_cameraBinding == null || !isAdded) return@runOnUiThread

            cameraBinding.overlay.clear()
            cameraBinding.progress.visibility = View.VISIBLE
            cameraManager?.clearAnalyzer()
        }
    }

    override fun onError(error: String) {
        super.onError(error)

    }

    override fun onDetect(
        results: Array<DetectionResult>,
        inferenceTime: Long,
        detectWidth: Int, detectHeight: Int,
        inputImage: Bitmap,
    ) {
        super.onDetect(results, inferenceTime, detectWidth, detectHeight, inputImage)
        activity?.runOnUiThread {

            if (_cameraBinding == null || !isAdded) return@runOnUiThread

            try {
                cameraBinding.inferenceTime.text = "Inference Time : $inferenceTime ms"

                cameraBinding.overlay.clear()
                cameraBinding.overlay.apply {
                    setResults(
                        results,
                        Pair(detectHeight, detectWidth),
                        Pair(cameraBinding.viewFinder.height, cameraBinding.viewFinder.width)
                    )
                }
            } catch (e: Exception) {
                Log.e(TAG, "onDetectError: ${e.message}")
            }

        }
    }

    override fun onEmptyDetect() {
        super.onEmptyDetect()
        activity?.runOnUiThread {
            if (_cameraBinding == null || !isAdded) return@runOnUiThread

            cameraBinding.overlay.clear()
        }
    }

    companion object {
        private const val TAG = "InstantCameraFragment"
    }
}
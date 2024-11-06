package com.example.duriannet.presentation.detector.fragments.instant_detect

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.lifecycleScope
import com.example.duriannet.R
import com.example.duriannet.models.DetectionResult
import com.example.duriannet.presentation.detector.view_models.DetectorViewModel
import com.example.duriannet.services.detector.interfaces.IDetectorListener
import kotlinx.coroutines.launch

abstract class BaseInstantDetectFragment : Fragment(), IDetectorListener {

    protected val viewModel: DetectorViewModel by hiltNavGraphViewModels(R.id.detector_nav_graph)


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.e(TAG, "parentFragment: $parentFragment")

        val hostFragment = parentFragment as InstantDetectViewPagerFragment

        hostFragment.setIouThresholdListener { iouThreshold ->
            viewModel.setIouThreshold(iouThreshold)
        }

        hostFragment.setConfidenceThresholdListener { cnfThreshold ->
            viewModel.setConfidenceThreshold(cnfThreshold)
        }

        hostFragment.setMaxNumberDetectionListener { numThreads ->
            viewModel.setMaxNumberDetection(numThreads)
        }

        hostFragment.setOnModelSelectedListener { position, id ->
            when (position) {
                0 -> {
                    viewModel.setDetectionModel(
                        false,
                        this@BaseInstantDetectFragment,
                        requireContext()
                    )
                }

                1 -> {
                    viewModel.setDetectionModel(
                        true,
                        this@BaseInstantDetectFragment,
                        requireContext()
                    )
                }
            }
        }

        // set default values
        viewModel.setConfidenceThreshold(resources.getString(R.string.default_confidence_threshold).toFloat())
        viewModel.setIouThreshold(resources.getString(R.string.default_iou_threshold).toFloat())
        viewModel.setMaxNumberDetection(resources.getString(R.string.default_max_number_detection).toInt())

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.startDetector(
                false,
                this@BaseInstantDetectFragment,
                requireContext()
            )
        }

        Log.e(TAG, "onViewCreated")
    }

    override fun onResume() {
        super.onResume()
        // update listener, because the fragment might be changed
        viewModel.updateListener(this)
        Log.e(TAG, "onResume")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        lifecycleScope.launch {
            viewModel.stopDetector()
        }

        Log.e(TAG, "onDestroyView")
        Toast.makeText(context, "Detector destroyed", Toast.LENGTH_SHORT).show()
    }

    override fun onInitialized() {
        // no ops
    }

    override fun onStopped() {
        // no ops
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Log.e(TAG, "onError : $error")
            Toast.makeText(requireActivity(), "Error: $error", Toast.LENGTH_LONG).show()
        }
        // no ops
    }

    override fun onDetect(results: Array<DetectionResult>, inferenceTime: Long, detectWidth: Int, detectHeight: Int, inputImage: Bitmap) {
        // no ops
    }

    override fun onEmptyDetect() {
        // no ops
    }


    companion object {
        private val TAG = BaseInstantDetectFragment::class.simpleName
    }
}
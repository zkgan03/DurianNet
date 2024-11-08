package com.example.duriannet.presentation.detector.fragments.instant_detect

import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.duriannet.models.DetectionResult
import com.example.duriannet.presentation.detector.view_models.DetectorViewModel
import com.example.duriannet.services.detector.enum.DetectorStatusEnum
import com.example.duriannet.services.detector.interfaces.IDetectorListener

abstract class BaseInstantDetectFragment : Fragment(), IDetectorListener {

    protected val viewModel: DetectorViewModel by viewModels(
        ownerProducer = { requireParentFragment() }
    )

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

        if (viewModel.detector == null || viewModel.detector?.status() != DetectorStatusEnum.INITIALIZING)
            viewModel.startDetector(
                false,
                this,
                requireContext()
            )

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
        viewModel.stopDetector()
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
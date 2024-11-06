package com.example.duriannet.presentation.detector.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.example.duriannet.databinding.FragmentDetectorEntryBinding


class DetectorEntryFragment : Fragment() {


    private var _binding: FragmentDetectorEntryBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentDetectorEntryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
    }

    private fun setupUI() {

        val navController = findNavController()

        binding.btnStartInstantDetect.setOnClickListener {
            // Navigate to Instant Detection Fragment
            navController.navigate(DetectorEntryFragmentDirections.actionDetectorEntryFragmentToInstantDetectViewPagerFragment())
        }

        binding.btnStartFocusVision.setOnClickListener {
            // Navigate to Focus Vision Fragment

        }

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private val TAG = DetectorEntryFragment::class.java.simpleName
    }
}
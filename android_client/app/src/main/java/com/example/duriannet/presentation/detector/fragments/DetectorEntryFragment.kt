package com.example.duriannet.presentation.detector.fragments

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.navigation.fragment.findNavController
import com.example.duriannet.databinding.FragmentDetectorEntryBinding
import com.example.duriannet.utils.Common.hasPermissions

private val PERMISSIONS_REQUIRED =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_VISUAL_USER_SELECTED
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.READ_MEDIA_IMAGES,
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
        )
    }


class DetectorEntryFragment : Fragment() {
    private var _binding: FragmentDetectorEntryBinding? = null
    private val binding get() = _binding!!

    private val requestAllPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            if (!hasPermissions(requireContext(), PERMISSIONS_REQUIRED)) {
                Toast.makeText(requireContext(), "Permission request denied", Toast.LENGTH_LONG).show()
                requireActivity().onBackPressedDispatcher.onBackPressed()
            }
        }

    override fun onResume() {
        super.onResume()

        if (!hasPermissions(requireContext(), PERMISSIONS_REQUIRED)) {
            requestAllPermissionLauncher.launch(PERMISSIONS_REQUIRED)
        }
    }


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
            navController.navigate(DetectorEntryFragmentDirections.actionDetectorEntryFragmentToFocusVisionFragment())
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
package com.example.duriannet.presentation.detector.fragments.focus_vision

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.fragment.findNavController
import androidx.navigation.navGraphViewModels
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentFocusVisionResultBinding
import com.example.duriannet.presentation.detector.view_models.FocusVisionViewModel
import com.example.duriannet.utils.Common

class FocusVisionResultFragment : Fragment() {

    private var _binding: FragmentFocusVisionResultBinding? = null;
    private val binding get() = _binding!!

    private val viewModel: FocusVisionViewModel by navGraphViewModels(R.id.detector_nav_graph)

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentFocusVisionResultBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar()
        setupUI()
    }

    private fun setupUI() {
        binding.imageResult.setImageBitmap(viewModel.getImageResult())

        binding.fabDownload.setOnClickListener {
            try {
                Common.saveBitmap(
                    requireContext(),
                    "focus_vision_result_" + System.currentTimeMillis(),
                    viewModel.getImageResult()!!
                )

                Toast.makeText(requireContext(), "Image saved !", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
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
    }


    companion object {
        private val TAG = "FocusVisionResultFragment"
    }

}
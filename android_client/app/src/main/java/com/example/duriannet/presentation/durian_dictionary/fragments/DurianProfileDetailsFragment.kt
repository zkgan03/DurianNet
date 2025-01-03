package com.example.duriannet.presentation.durian_dictionary.fragments

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentDurianProfileDetailsBinding
import com.example.duriannet.presentation.durian_dictionary.view_models.DurianProfileDetailsViewModel
import com.example.duriannet.utils.Constant
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DurianProfileDetailsFragment : Fragment() {

    private var _binding: FragmentDurianProfileDetailsBinding? = null
    private val binding get() = _binding!!

    private val viewModel: DurianProfileDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDurianProfileDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve durianId using Safe Args
        val durianId = arguments?.let { DurianProfileDetailsFragmentArgs.fromBundle(it).durianId }
        if (durianId == null) {
            Log.e("DurianProfileDetailsFragment", "Invalid or missing durianId")
            return
        }

        // Load durian details from ViewModel
        viewModel.loadDurianDetails(durianId)

        // Observe the state and update UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.durianDetailsState.collect { durian ->
                if (durian == null) {
                    Log.e("DurianProfileDetailsFragment", "Durian details not loaded")
                    return@collect
                }

                // Log and set text fields
                Log.d("DurianProfileDetailsFragment", "Durian details loaded: $durian")
                binding.txtDurianName.text = durian.durianName
                binding.txtDurianCode.text = durian.durianCode
                binding.txtDurianDescription.text = durian.durianDescription
                binding.txtDurianCharacteristic.text = durian.characteristics
                binding.txtDurianTaste.text = durian.tasteProfile

                // Resolve and load image
                val baseUrl = Constant.SERVER_BASE_URL
                val fullImageUrl = if (durian.durianImage.startsWith("http")) {
                    durian.durianImage
                } else {
                    "$baseUrl${if (durian.durianImage.startsWith("/")) durian.durianImage else "/${durian.durianImage}"}"
                }
                Glide.with(requireContext())
                    .load(fullImageUrl)
                    .placeholder(R.drawable.unknownuser)
                    .error(R.drawable.unknownuser)
                    .into(binding.durianImage)

                // Load video
                if (!durian.durianVideoUrl.isNullOrEmpty()) {
                    val fullVideoUrl = if (durian.durianVideoUrl.startsWith("http")) {
                        durian.durianVideoUrl
                    } else {
                        "$baseUrl${if (durian.durianVideoUrl.startsWith("/")) durian.durianVideoUrl else "/${durian.durianVideoUrl}"}"
                    }
                    val mediaController = MediaController(requireContext())
                    mediaController.setAnchorView(binding.vvDurianVideo)
                    binding.vvDurianVideo.apply {
                        setMediaController(mediaController)
                        setVideoURI(Uri.parse(fullVideoUrl))
                        setOnPreparedListener { start() }
                    }
                } else {
                    binding.vvDurianVideo.visibility = View.GONE
                }

                // Set video description
                if (!durian.durianVideoDescription.isNullOrEmpty()) {
                    binding.txtVideoDescription.text = durian.durianVideoDescription
                    binding.txtVideoDescription.visibility = View.VISIBLE
                } else {
                    binding.txtVideoDescription.visibility = View.GONE
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


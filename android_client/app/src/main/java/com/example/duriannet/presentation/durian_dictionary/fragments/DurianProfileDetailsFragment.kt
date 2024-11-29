package com.example.duriannet.presentation.durian_dictionary.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.duriannet.databinding.FragmentDurianProfileDetailsBinding
import com.example.duriannet.presentation.durian_dictionary.view_models.DurianProfileDetailsViewModel
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

        val durianId = arguments?.getString("durianId")?.toInt() ?: return
        viewModel.loadDurianDetails(durianId)

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.durianDetailsState.collect { durian ->
                durian?.let {
                    binding.txtDurianName.text = it.durianName
                    binding.txtDurianCode.text = it.durianCode
                    binding.txtDurianDescription.text = it.durianDescription
                    binding.txtDurianCharacteristic.text = it.characteristics
                    binding.txtDurianTaste.text = it.tasteProfile
                    Glide.with(requireContext()).load(it.durianImage).into(binding.durianImage)
                    binding.vvDurianVideo.setVideoURI(Uri.parse(it.durianVideoUrl))
                    binding.vvDurianVideo.start()
                    binding.txtVideoDescription.text = it.durianVideoDescription
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

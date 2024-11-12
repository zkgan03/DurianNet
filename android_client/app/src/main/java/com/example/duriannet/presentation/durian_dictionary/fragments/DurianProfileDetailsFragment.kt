package com.example.duriannet.presentation.durian_dictionary.fragments

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.example.duriannet.databinding.FragmentDurianProfileDetailsBinding
import com.example.duriannet.presentation.durian_dictionary.view_models.DurianProfileDetailsViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DurianProfileDetailsFragment : Fragment() {
    private var _binding: FragmentDurianProfileDetailsBinding? = null
    private val binding get() = _binding!!

    private val durianProfileDetailsViewModel: DurianProfileDetailsViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDurianProfileDetailsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val durianId = arguments?.getString("durianId") ?: return
        durianProfileDetailsViewModel.loadDurianDetails(durianId)

        viewLifecycleOwner.lifecycleScope.launch {
            durianProfileDetailsViewModel.durianDetailsState.collect { durian ->
                durian?.let {
                    binding.txtDurianName.text = it.name
                    binding.txtDurianDescription.text = it.description
                    binding.txtDurianCharacteristic.text = it.characteristic
                    binding.txtDurianTaste.text = it.tasteProfile
                    binding.vvDurianVideo.setVideoURI(Uri.parse(it.videoUrl))
                    binding.vvDurianVideo.start()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
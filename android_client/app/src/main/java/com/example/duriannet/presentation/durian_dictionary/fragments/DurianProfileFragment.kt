package com.example.duriannet.presentation.durian_dictionary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.duriannet.databinding.FragmentDurianProfileBinding
import com.example.duriannet.presentation.durian_dictionary.view_models.DurianProfileViewModel
import com.example.duriannet.presentation.account_management.adapter.DurianProfileAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DurianProfileFragment : Fragment() {
    private var _binding: FragmentDurianProfileBinding? = null
    private val binding get() = _binding!!

    private val durianProfileViewModel: DurianProfileViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDurianProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = DurianProfileAdapter()
        binding.rvDurianProfile.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDurianProfile.adapter = adapter

        durianProfileViewModel.loadAllDurians()

        viewLifecycleOwner.lifecycleScope.launch {
            durianProfileViewModel.durianProfileState.collect { durians ->
                adapter.submitList(durians.map { it.name }) // Assuming Durian has a 'name' property
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
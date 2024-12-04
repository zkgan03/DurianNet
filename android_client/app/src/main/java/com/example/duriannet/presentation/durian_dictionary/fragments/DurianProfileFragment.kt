package com.example.duriannet.presentation.durian_dictionary.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.duriannet.databinding.FragmentDurianProfileBinding
import com.example.duriannet.presentation.durian_dictionary.adapter.DurianProfileAdapter
import com.example.duriannet.presentation.durian_dictionary.view_models.DurianProfileViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DurianProfileFragment : Fragment() {

    private var _binding: FragmentDurianProfileBinding? = null
    private val binding get() = _binding!!
    private val viewModel: DurianProfileViewModel by viewModels()

    private lateinit var adapter: DurianProfileAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDurianProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = DurianProfileAdapter { durian ->
            val action = DurianProfileFragmentDirections.actionDurianProfileToDetails(durian.durianId)
            findNavController().navigate(action)
        }

        binding.rvDurianProfile.layoutManager = LinearLayoutManager(requireContext())
        binding.rvDurianProfile.adapter = adapter

        viewModel.loadAllDurians()

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.durianProfileState.collect { state ->
                if (state.error.isNotEmpty()) {
                    Log.e("DurianProfileFragment", "Error: ${state.error}")
                } else {
                    Log.d("DurianProfileFragment", "Durians loaded: ${state.filteredDurians.size}")
                    if (state.filteredDurians.isEmpty()) {
                        //binding.emptyStateView.visibility = View.VISIBLE
                        binding.rvDurianProfile.visibility = View.GONE
                    } else {
                        //binding.emptyStateView.visibility = View.GONE
                        binding.rvDurianProfile.visibility = View.VISIBLE
                        adapter.submitList(state.filteredDurians)
                    }
                }
            }
        }


        binding.svDurianProfile.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.filterDurians(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterDurians(newText.orEmpty())
                return true
            }
        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

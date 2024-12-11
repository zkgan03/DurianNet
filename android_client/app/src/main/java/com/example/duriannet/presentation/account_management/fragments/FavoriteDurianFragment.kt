package com.example.duriannet.presentation.account_management.fragments

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.duriannet.databinding.FragmentFavoriteDurianBinding
import com.example.duriannet.presentation.account_management.adapter.FavoriteDurianSelectionAdapter
import com.example.duriannet.presentation.account_management.view_models.FavoriteDurianViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import android.widget.Toast
import kotlinx.coroutines.delay

@AndroidEntryPoint
class FavoriteDurianFragment : Fragment() {

    private var _binding: FragmentFavoriteDurianBinding? = null
    private val binding get() = _binding!!
    private val viewModel: FavoriteDurianViewModel by viewModels()
    private val navController by lazy { findNavController() }
    private lateinit var adapter: FavoriteDurianSelectionAdapter
    private lateinit var username: String
    private var isInitialLoad = true // Track if it's the initial load

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteDurianBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sharedPreferences = requireActivity().getSharedPreferences("DurianNetPrefs", Context.MODE_PRIVATE)
        username = sharedPreferences.getString("username", "") ?: ""

        // Hide the SearchView programmatically
        binding.svFavoriteDurian.visibility = View.GONE

        adapter = FavoriteDurianSelectionAdapter { durian, isFavorite ->
            viewModel.onFavoriteChange(durian.durianId, isFavorite)
        }

        binding.rvFavoriteDurianProfile.layoutManager = LinearLayoutManager(context)
        binding.rvFavoriteDurianProfile.adapter = adapter

        if (username.isNotEmpty()) {
            viewModel.loadDurians(username)
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.favoriteDurianState.collect { state ->
                if (state.error.isNotEmpty()) {
                    Toast.makeText(requireContext(), state.error, Toast.LENGTH_SHORT).show()
                } else {
                    println("Filtered Durians Count: ${state.filteredDurians.size}")
                    state.filteredDurians.forEach { println("Durian: ${it.durianName}") }

                    adapter.submitList(state.filteredDurians, state.favoriteDurianIds)

                    // Show "No results found" only after the initial load
                    if (!isInitialLoad && state.filteredDurians.isEmpty()) {
                        Toast.makeText(requireContext(), "No results found", Toast.LENGTH_SHORT).show()
                    }

                    // Mark initial load as completed
                    isInitialLoad = false

                }
            }
        }


        // Handle SearchView
        binding.svFavoriteDurian.setOnQueryTextListener(object : android.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                viewModel.filterDurians(query.orEmpty())
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                viewModel.filterDurians(newText.orEmpty())
                return true
            }
        })

        // Save Button Click Listener
        binding.btnFdSave.setOnClickListener {
            lifecycleScope.launch {
                viewModel.saveFavoriteChanges(username)
                delay(500) // Wait for 1 second to give server time to process
                Toast.makeText(requireContext(), "Favorites saved successfully", Toast.LENGTH_SHORT).show()

                // Set a result to notify ProfileFragment to reload its data
                parentFragmentManager.setFragmentResult("favorite_updated", Bundle())
                navController.navigateUp()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}



package com.example.duriannet.presentation.account_management.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentFavoriteDurianBinding
import com.example.duriannet.presentation.account_management.view_models.FavoriteDurianViewModel
import com.example.duriannet.presentation.account_management.adapter.AllDurianAdapter
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FavoriteDurianFragment : Fragment() {
    private var _binding: FragmentFavoriteDurianBinding? = null
    private val binding get() = _binding!!

    private val favoriteDurianViewModel: FavoriteDurianViewModel by viewModels()
    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoriteDurianBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = AllDurianAdapter()
        binding.rvFavoriteDurianProfile.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavoriteDurianProfile.adapter = adapter

        favoriteDurianViewModel.loadAllDurians()

        viewLifecycleOwner.lifecycleScope.launch {
            favoriteDurianViewModel.favoriteDurianState.collect { state ->
                if (state.error.isNotEmpty()) {
                    // Handle error
                } else {
                    adapter.submitList(state.favoriteDurians)
                }
            }
        }

        binding.btnFdSave.setOnClickListener {
            navController.navigate(R.id.action_favorite_durian_to_profile)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
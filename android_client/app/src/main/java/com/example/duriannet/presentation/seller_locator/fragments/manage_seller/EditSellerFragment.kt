package com.example.duriannet.presentation.seller_locator.fragments.manage_seller

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.children
import androidx.core.widget.doOnTextChanged
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentEditSellerBinding
import com.example.duriannet.models.DurianType
import com.example.duriannet.presentation.seller_locator.view_models.ManageSellerViewModel
import com.example.duriannet.utils.Common
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class EditSellerFragment : Fragment() {

    private var _binding: FragmentEditSellerBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ManageSellerViewModel by hiltNavGraphViewModels(R.id.manage_seller_nav_graph)

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentEditSellerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar()
        setupUI()
        setupObservers()
    }

    private fun setupObservers() {


    }

    private fun setupActionBar() {

        val parentActivity = requireActivity() as AppCompatActivity

        parentActivity.setSupportActionBar(binding.toolbar)

        parentActivity.supportActionBar?.apply {
            title = "Edit Seller"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            navController.navigateUp()
        }

        parentActivity.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.detector_menu, menu)
                    menu.findItem(R.id.action_done).isVisible = true
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_done -> {
                            viewModel.updateSeller {
                                requireActivity().runOnUiThread {
                                    navController.navigateUp()
                                }
                            }
                            true
                        }

                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

    }

    private fun setupUI() {

        val sellerImage = viewModel.selectedSellerState.value?.imagePath
        Common.loadServerImageIntoView(requireContext(), sellerImage, binding.imageViewDetectedImage)

        binding.chipGroupDurianTypes.removeAllViews()
        binding.chipGroupDurianTypes.apply {
            //add chips for each durian type
            DurianType.entries.forEach { durianType ->
                val chip = layoutInflater.inflate(R.layout.chip_durian_type, this, false) as Chip
                chip.text = durianType.name
                // check the chip if the seller has this durian type
//                chip.isChecked = viewModel.selectedSellerState.value?.durianTypes?.contains(durianType) ?: false
                addView(chip)
            }

        }

        binding.editTextSellerName.setText(viewModel.selectedSellerState.value?.name)
        binding.editTextDescription.setText(viewModel.selectedSellerState.value?.description)

        // check the chip if the seller has this durian type
        binding.chipGroupDurianTypes.children.forEach { chip ->
            val durianType = DurianType.valueOf((chip as Chip).text.toString())
            chip.isChecked = viewModel.selectedSellerState.value?.durianTypes!!.contains(durianType)
        }

        binding.editTextSellerName.doOnTextChanged { text, _, _, _ ->
            viewModel.inputSellerName(text.toString())
        }

        binding.editTextDescription.doOnTextChanged { text, _, _, _ ->
            viewModel.inputSellerDescription(text.toString())
        }

        binding.chipGroupDurianTypes.setOnCheckedStateChangeListener { chipGroup, chipIds ->
            val selectedDurianTypes =
                chipIds
                    .map { DurianType.valueOf(chipGroup.findViewById<Chip>(it).text.toString()) }
                    .toSet()

            viewModel.inputSellerDurianType(selectedDurianTypes)
        }
    }

}
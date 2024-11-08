package com.example.duriannet.presentation.seller_locator.fragments.add_seller

import android.os.Bundle
import android.util.Log
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
import androidx.navigation.fragment.findNavController
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentAddSellerBinding
import com.example.duriannet.models.DurianType
import com.example.duriannet.models.Seller
import com.example.duriannet.presentation.seller_locator.view_models.AddSellerViewModel
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.chip.Chip
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AddSellerFragment : Fragment() {

    private var _binding: FragmentAddSellerBinding? = null
    val binding get() = _binding!!

    private val viewModel: AddSellerViewModel by hiltNavGraphViewModels(R.id.add_seller_nav_graph)

    private val navController by lazy { findNavController() }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentAddSellerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupActionBar()
        setupUI()
    }

    private fun setupActionBar() {

        val parentActivity = requireActivity() as AppCompatActivity

        parentActivity.setSupportActionBar(binding.toolbar)

        parentActivity.supportActionBar?.apply {
            title = "Add a New Seller"
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

                            viewModel.addSeller {
                                requireActivity().runOnUiThread {
                                    navController.navigate(R.id.action_addSellerFragment_to_manage_seller_nav_graph)
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
        binding.imageViewDetectedImage.setImageBitmap(viewModel.imageResult)

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

        binding.editTextSellerName.setText(viewModel.inputState.value.sellerName)
        binding.editTextDescription.setText(viewModel.inputState.value.sellerDescription)
        binding.chipGroupDurianTypes.children.forEach { chip ->
            val durianType = DurianType.valueOf((chip as Chip).text.toString())
            chip.isChecked = viewModel.inputState.value.sellerDurianType.contains(durianType)
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

    override fun onResume() {
        super.onResume()
        Log.e(TAG, "OnResume")
    }

    override fun onStop() {
        super.onStop()
        Log.e(TAG, "OnStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.e(TAG, "OnDestroy")
    }

    override fun onPause() {
        super.onPause()
        Log.e(TAG, "OnPause")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.e(TAG, "OnDestroyView")
        _binding = null
    }

    companion object {
        private const val TAG = "AddSellerFragment"
    }
}
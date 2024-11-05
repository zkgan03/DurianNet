package com.example.duriannet.presentation.seller_locator.fragments

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.widget.doOnTextChanged
import androidx.hilt.navigation.fragment.hiltNavGraphViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentManageSellerBinding
import com.example.duriannet.models.Seller
import com.example.duriannet.presentation.seller_locator.adapter.AddedSellersAdapter
import com.example.duriannet.presentation.seller_locator.view_models.ManageSellerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ManageSellerFragment : Fragment() {

    private var _binding: FragmentManageSellerBinding? = null
    private val binding get() = _binding!!

    private val addedSellersAdapter by lazy { AddedSellersAdapter() }

    private val navController by lazy { findNavController() }

    private val viewModel: ManageSellerViewModel by hiltNavGraphViewModels(R.id.manage_seller_nav_graph)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentManageSellerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupBackPress()
        setupActionBar()
        setupSellerAddedRecyclerView()
        setupSearchView()
    }

    private fun setupSearchView() {

        // TODO : setup to make the icon clickable, or reusing the same adapter
        val adapterResult = AddedSellersAdapter()
        binding.recyclerResults.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = adapterResult
        }

        binding.searchView.editText.doOnTextChanged { text, start, before, count ->
            adapterResult.submitList(viewModel.filterAddedSeller(text.toString()))
        }

    }

    private fun setupBackPress() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            if (binding.searchView.isShowing) {
                binding.searchView.hide()
            } else {
                navController.navigateUp()
            }
        }
    }

    private fun setupActionBar() {

        val parent = requireActivity() as AppCompatActivity

        parent.setSupportActionBar(binding.toolbar)
        parent.supportActionBar?.setDisplayHomeAsUpEnabled(true)

        parent.addMenuProvider(
            object : MenuProvider {
                override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                    menuInflater.inflate(R.menu.detector_menu, menu)
                    menu.findItem(R.id.action_search).isVisible = true
                }

                override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                    return when (menuItem.itemId) {
                        R.id.action_search -> {
                            Toast.makeText(requireContext(), "Search clicked", Toast.LENGTH_SHORT).show()
                            binding.searchView.show()
                            true
                        }

                        else -> false
                    }
                }
            },
            viewLifecycleOwner,
            Lifecycle.State.RESUMED
        )

        binding.toolbar.apply {
            title = "Seller Added"
        }
    }

    private fun setupSellerAddedRecyclerView() {

        addedSellersAdapter.setOnEditClickedListener { seller: Seller ->
            viewModel.selectSeller(seller)
            navController.navigate(R.id.action_manageSellerFragment_to_editSellerFragment)
        }

        addedSellersAdapter.setOnDeleteClickedListener { seller: Seller ->
            val builder = AlertDialog.Builder(requireContext())
            builder.setMessage("Are you confirm to delete ${seller.name}?")
                .setCancelable(false)
                .setPositiveButton("Delete") { dialog, id ->

                    viewModel.removeSeller(seller)

                    // Delete the seller
                    Toast.makeText(requireContext(), "Seller ${seller.sellerId} deleted", Toast.LENGTH_SHORT).show()
                    dialog.dismiss()
                }
                .setNegativeButton("Cancel") { dialog, id ->
                    dialog.dismiss()
                }

            val alert = builder.create()
            alert.show()

            // Customize the message text
            val messageView = alert.findViewById<TextView>(android.R.id.message)
            messageView?.setTextColor(Color.BLACK)
            messageView?.setTypeface(null, Typeface.BOLD)

            // Customize the button text colors
            alert.getButton(AlertDialog.BUTTON_NEGATIVE)?.setTextColor(Color.BLACK)
            alert.getButton(AlertDialog.BUTTON_POSITIVE)?.setTextColor(Color.RED)

        }

        binding.recyclerViewSellerAdded.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = addedSellersAdapter
        }

        // TODO: Replace with actual data, get from view model
        lifecycleScope.launch {
            viewModel
                .addedSellers
                .distinctUntilChangedBy { it }
                .collectLatest { sellers ->
                    addedSellersAdapter.submitList(sellers)
                }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val TAG = "ManageSellerFragment"
    }
}
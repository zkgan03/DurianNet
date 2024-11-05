package com.example.duriannet.presentation.seller_locator.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.duriannet.databinding.FragmentSearchBinding
import com.example.duriannet.presentation.seller_locator.adapter.SearchResultsAdapter
import com.example.duriannet.utils.Common

/**
 * Reusable search fragment
 *
 * NOTE :
 * Ensure to make use of the onViewCreatedListener in order to set everything up, this is because
 * the fragment lifecycle is not guaranteed to be in the started state when the fragment is created (binding might be null)
 **/

class SearchFragment : Fragment() {

    private var _binding: FragmentSearchBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.e(TAG, "OnCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        Log.e(TAG, "OnCreateView")
        _binding = FragmentSearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.e(TAG, "OnViewCreated")

        binding.apply {
            searchView.editText.doOnTextChanged { text, start, before, count ->
                binding.searchBar.setText(text.toString())
                onSearchViewInput?.invoke(text.toString())
            }

            searchView.editText.setOnEditorActionListener { textView, i, keyEvent ->
                val query = textView.text.toString()
                onSearchViewAction?.invoke(query)

                Common.hideKeyboard(requireActivity()) // hide keyboard

                return@setOnEditorActionListener true
            }
        }

        onViewCreated?.invoke()
    }

    private var onViewCreated: (() -> Unit)? = null
    fun setOnViewCreatedListener(listener: () -> Unit) {
        onViewCreated = listener
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private var onSearchViewInput: ((input: String) -> Unit)? = null
    fun setOnSearchViewInputListener(listener: (String) -> Unit) {
        onSearchViewInput = listener
    }

    private var onSearchViewAction: ((input: String) -> Unit)? = null
    fun setOnSearchViewActionListener(listener: (String) -> Unit) {
        onSearchViewAction = listener
    }

    fun hideSearchView() {
        binding.searchView.hide()
    }

    fun setSearchViewAdapter(adapter: SearchResultsAdapter) {
        Log.e(TAG, "current lifecycle state: ${lifecycle.currentState}")
        binding.recyclerResults.adapter = adapter
    }

    fun setSearchViewLayoutManager(layoutManager: LinearLayoutManager) {
        binding.recyclerResults.layoutManager = layoutManager
    }

    fun setSearchBarText(text: String) {
        binding.searchBar.setText(text)
    }

    fun setSearchHint(hint: String) {
        binding.searchView.editText.hint = hint
        binding.searchBar.hint = hint
    }

    companion object {
        const val TAG = "SearchFragment"
    }
}
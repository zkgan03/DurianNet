package com.example.duriannet.presentation.seller_locator.fragments.map_locator

import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.activity.addCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentCommentBinding
import com.example.duriannet.presentation.seller_locator.state.CommentData
import com.example.duriannet.utils.Common

class CommentChildFragment : Fragment() {

    private var _binding: FragmentCommentBinding? = null
    private val binding get() = _binding!!

    private var commentData: CommentData? = null

    companion object {
        fun newInstance(commentData: CommentData? = null) = CommentChildFragment().apply {
            this.commentData = commentData
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _binding = FragmentCommentBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupActionBar()
        setupBackPress()

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setupBackPress() {
        requireActivity()
            .onBackPressedDispatcher
            .addCallback(viewLifecycleOwner) {
                onBackClicked?.invoke()
                parentFragmentManager.popBackStack()
            }
    }

    private fun setupActionBar() {

        val parentActivity = requireActivity() as AppCompatActivity

        parentActivity.setSupportActionBar(binding.toolbar)

        parentActivity.supportActionBar?.apply {
            title = if (commentData?.isEdit == true) "Edit Comment" else "Add a New Comment"
            setDisplayHomeAsUpEnabled(true)
        }

        binding.toolbar.setNavigationOnClickListener {
            //finish current fragment
            onBackClicked?.invoke()
            parentFragmentManager.popBackStack()
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
                            val comment = binding.editTextComment.text.toString()
                            val rating = binding.ratingBar.rating

                            onDoneClicked?.invoke(comment, rating)

                            parentFragmentManager.popBackStack()

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
        binding.apply {
            //show keyboard
            editTextComment.requestFocus()
            Common.showKeyboard(requireActivity(), editTextComment)
        }


        // Set initial values if editing
        commentData?.let { data ->
            binding.editTextComment.setText(data.content)
            binding.ratingBar.rating = data.rating
        }
    }

    private var onDoneClicked: ((comment: String, rating: Float) -> Unit)? = null
    fun setOnDoneClickListener(listener: (String, Float) -> Unit) {
        onDoneClicked = listener
    }

    private var onBackClicked: (() -> Unit)? = null
    fun setOnBackClickListener(listener: () -> Unit) {
        onBackClicked = listener
    }

}
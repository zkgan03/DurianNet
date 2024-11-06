package com.example.duriannet.presentation.detector.fragments.instant_detect

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.view.GestureDetector
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.example.duriannet.R
import com.example.duriannet.databinding.FragmentInstantGalleryBinding
import com.example.duriannet.models.MediaStoreData
import com.example.duriannet.presentation.detector.adapters.GalleryItemAdapter
import com.example.duriannet.services.common.MediaStoreDataSource
import com.google.android.material.bottomsheet.BottomSheetDialog
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class InstantGalleryFragment : BaseInstantDetectFragment() {

    private var _galleryBinding: FragmentInstantGalleryBinding? = null
    private val galleryBinding get() = _galleryBinding!!

    private lateinit var mediaStoreDataSource: MediaStoreDataSource

//    private val detectorViewModel: DetectorViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        _galleryBinding = FragmentInstantGalleryBinding.inflate(inflater, container, false)
        mediaStoreDataSource = MediaStoreDataSource(requireContext())

        return galleryBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // setup gallery dialog
        setupBottomSheetDialog()

        if (viewModel.isDetectorInitialized) {
            galleryBinding.progress.visibility = View.GONE
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setupBottomSheetDialog() {
        val bottomSheetDialog = BottomSheetDialog(requireContext())
        bottomSheetDialog.behavior.halfExpandedRatio = 0.6f

        val view1 = LayoutInflater.from(requireContext())
            .inflate(R.layout.bottom_sheet_gallery, null)
            .also { bottomSheetDialog.setContentView(it) }


        //setup recyclerview
        val dialogRecyclerView: RecyclerView = view1.findViewById(R.id.galleryRecyclerView)

        val adapter = GalleryItemAdapter() // Adapter for the dialog recyclerview
        adapter.setOnMediaSelectedListener { media ->
            bottomSheetDialog.dismiss()
            onMediaSelected(media)
        }
        val images = mediaStoreDataSource.loadImages() // Load images from MediaStore, and observe the changes
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                images.collectLatest {
                    adapter.submitList(it)
                }

            }
        }

        dialogRecyclerView.layoutManager = GridLayoutManager(requireContext(), 3)
        dialogRecyclerView.adapter = adapter

        galleryBinding.fabGetContent.setOnClickListener {
            bottomSheetDialog.show()
        }

        // Gesture detection
        val gestureDetector = GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
            override fun onScroll(e1: MotionEvent?, e2: MotionEvent, distanceX: Float, distanceY: Float): Boolean {
                if (e1 != null) {
                    if (e1.y > e2.y) {
                        // Swiped up
                        Log.d("Gesture", "Swipe up detected")
                        bottomSheetDialog.show()
                        return true
                    }
                }
                return super.onScroll(e1, e2, distanceX, distanceY)
            }
        })
        galleryBinding.fragmentGalleryContainer.setOnTouchListener { _, event ->
            gestureDetector.onTouchEvent(event)
            true
        }

        //will trigger when dialog is dismissed / closed
        bottomSheetDialog.setOnDismissListener {
//            Toast.makeText(requireContext(), "Dialog Dismissed", Toast.LENGTH_SHORT).show()
        }

    }


    private fun onMediaSelected(media: MediaStoreData) {
        Log.e(TAG, "onMediaSelected: $, ${media.uri}")

        galleryBinding.progress.visibility = View.VISIBLE

        // Load the image from the uri
        Glide.with(requireContext())
            .asBitmap()
            .load(media.uri)
            .into(object : CustomTarget<Bitmap>() {

                override fun onResourceReady(resource: Bitmap, transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?) {

                    val (results, height, width) = viewModel.detector!!.detectImage(resource)

                    galleryBinding.imageResult.setImageBitmap(resource)

                    // Desired maximum dimensions
                    val maxWidth = galleryBinding.imageResult.width
                    val maxHeight = galleryBinding.imageResult.height

                    Log.e(TAG, "maxWidth: $maxWidth, maxHeight $maxHeight")

                    // Calculate the aspect ratio
                    val aspectRatio = resource.width.toFloat() / resource.height.toFloat()

                    // Determine the width and height of image in in the image view
                    // while maintaining the aspect ratio
                    val (imgWidth, imgHeight) = when {
                        // landscape image
                        resource.width > resource.height -> {
                            Pair(maxWidth, (maxWidth / aspectRatio).toInt())
                        }

                        //portrait image
                        resource.width < resource.height -> {
                            Pair((maxHeight * aspectRatio).toInt(), maxHeight)
                        }

                        // square image
                        else -> {
                            if (maxWidth > maxHeight)
                                Pair(maxHeight, maxHeight)
                            else
                                Pair(maxWidth, maxWidth)
                        }
                    }

                    // Calculate the position to center the bounding box overlay
                    val offsetLeft = (maxWidth - imgWidth) / 2.0f
                    val offsetTop = (maxHeight - imgHeight) / 2.0f

                    galleryBinding.overlay.apply {
                        clear()
                        setResults(
                            detectionResults = results,
                            detectionSize = Pair(height, width),
                            oriSize = Pair(imgHeight, imgWidth), // the bounding box need to be scaled based on the imageview size
                            offset = Pair(offsetTop, offsetLeft),
                        )
                    }

                    galleryBinding.imageResult.visibility = View.VISIBLE
                    galleryBinding.progress.visibility = View.GONE
                    galleryBinding.videoView.visibility = View.GONE
                }

                override fun onLoadCleared(placeholder: android.graphics.drawable.Drawable?) {
                    // no ops
                }
            })



        galleryBinding.tvPlaceholder.visibility = View.GONE
    }


    override fun onInitialized() {
        activity?.runOnUiThread {
            _galleryBinding?.progress?.visibility = View.GONE
            Log.e(TAG, "onInitialized")
            Toast.makeText(requireActivity(), "Gallery Detector Initialized", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onError(error: String) {
        activity?.runOnUiThread {
            Toast.makeText(requireContext(), "Error: $error", Toast.LENGTH_LONG).show()
        }
    }

    override fun onEmptyDetect() {
        activity?.runOnUiThread {
            galleryBinding.progress.visibility = View.GONE
            Toast.makeText(requireContext(), "No object detected in image", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val TAG = "GalleryFragment"

    }
}
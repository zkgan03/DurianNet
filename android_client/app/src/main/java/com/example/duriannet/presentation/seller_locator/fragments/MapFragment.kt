package com.example.duriannet.presentation.seller_locator.fragments

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.duriannet.R
import com.example.duriannet.databinding.BottomSheetSellerBinding
import com.example.duriannet.databinding.FragmentMapBinding
import com.example.duriannet.databinding.ItemUserCommentedBinding
import com.example.duriannet.databinding.ItemUserNotCommentedBinding
import com.example.duriannet.utils.BitmapHelper
import com.example.duriannet.models.Seller
import com.example.duriannet.models.Comment
import com.example.duriannet.presentation.seller_locator.adapter.MarkerInfoWindowAdapter
import com.example.duriannet.presentation.seller_locator.adapter.SearchResultsAdapter
import com.example.duriannet.presentation.seller_locator.adapter.SellerCommentsAdapter
import com.example.duriannet.presentation.seller_locator.view_models.MapViewModel
import com.example.duriannet.services.common.GoogleMapManager
import com.example.duriannet.services.common.LocationAccessChecker
import com.example.duriannet.utils.Common
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


private val PERMISSIONS_REQUIRED =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    } else {
        arrayOf(
            Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
    }

@AndroidEntryPoint
class MapFragment : Fragment() {

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MapViewModel by viewModels()

    // request permission launcher
    private val requestAllPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { _ ->
            if (!Common.hasPermissions(requireContext(), PERMISSIONS_REQUIRED)) {
                Toast.makeText(requireContext(), "Permission request denied", Toast.LENGTH_LONG).show()
                requireActivity().onBackPressedDispatcher.onBackPressed() // close activity if permission denied
            } else {
                initMap()
            }
        }

    // Location access checker
    private lateinit var locationAccessChecker: LocationAccessChecker

    // Google Maps
    private lateinit var googleMapManager: GoogleMapManager<Seller>

    private val navController: NavController by lazy { findNavController() }


    private var isGpsSettingOpened = false
    private fun requestLocationAccess() {
        if (!isGpsSettingOpened) {
            isGpsSettingOpened = true
            // GPS is off, notify the user or take action
            Toast.makeText(requireContext(), "GPS is disabled", Toast.LENGTH_SHORT).show()
//            isGpsSettingOpened = true
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
            startActivity(intent)
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        super.onCreateView(inflater, container, savedInstanceState)
        _binding = FragmentMapBinding.inflate(inflater, container, false)

        return binding.root
    }

    private val adapter by lazy { SearchResultsAdapter() }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationAccessChecker = LocationAccessChecker(requireActivity())

        //
        //observe the states
        setupUI()
        setupStateObservers()
        initMap()
        setupBottomSheetDialog()

    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        bottomSheetDialog = null
        bottomSheetBinding = null
    }


    override fun onResume() {
        super.onResume()

        isGpsSettingOpened = false // when activity is resumed, the setting must be closed

        // Request permissions
        if (!Common.hasPermissions(requireContext(), PERMISSIONS_REQUIRED)) {
            requestAllPermissionLauncher.launch(PERMISSIONS_REQUIRED)
        } else {

            // observe GPS status after resumed (to get permission on location)
            locationAccessChecker.observeGpsStatus()
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChanged()
                .onEach { isGpsEnabled ->
                    if (!isGpsEnabled) {
                        requestLocationAccess()
                    } else {
                        isGpsSettingOpened = false
                    }
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)


        }
    }


    private fun setupStateObservers() {

        // collect search results
        viewModel.searchResults
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .distinctUntilChangedBy { it }
            .mapLatest { searchResults ->
                Log.e(TAG, "collect search results: $searchResults")
                adapter.submitList(searchResults)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        // collect query
        viewModel.query
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .distinctUntilChangedBy { it }
            .mapLatest { query ->
                Log.e(TAG, "collect query: $query")
                binding.searchBar.setText(query)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)


        // collect sellers
        viewModel.sellers
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .distinctUntilChangedBy { it }
            .mapLatest { sellers ->
                adapter.submitList(
                    sellers
                )
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        viewModel.userComment
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .mapLatest { userComment ->
                bottomSheetBinding?.userCommentSection?.removeAllViews()

                Log.e(TAG, "userComment: $userComment")

                if (userComment != null) {

                    Log.e(TAG, "Has user comment")
                    // show user comment in bottom sheet dialog
                    createUserCommentedView(userComment)


                } else {

                    Log.e(TAG, "No user comment")

                    // show user comment in bottom sheet dialog
                    createUserNotCommentedView()

                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

    private fun setupUI() {

        // SearchView setup
        // search results adapter
        adapter.setOnItemClickedListener { seller ->
            // item clicked
            Toast.makeText(requireContext(), "Item clicked: ${seller.name}", Toast.LENGTH_SHORT).show()

            viewModel.updateQuery(seller.name) // update query value
            binding.searchView.hide() // hide search view
            googleMapManager.moveToLocation(seller.latLng) // move to location
            googleMapManager.onSelect(seller) // select place
        }

        binding.apply {
            recyclerResults.adapter = adapter
            recyclerResults.layoutManager = LinearLayoutManager(requireContext())

            searchBar.hint = "Search for a seller"
            searchView.hint = "Search for a seller"

            searchView.editText.doOnTextChanged { text, start, before, count ->
                searchBar.setText(text.toString())
                viewModel.updateQueryAndSearch(text.toString())
            }

            searchView.editText.setOnEditorActionListener { textView, i, keyEvent ->
                val query = textView.text.toString()

                Common.hideKeyboard(requireActivity()) // hide keyboard

                return@setOnEditorActionListener true
            }

            fabAddSeller.setOnClickListener {
                navController.navigate(R.id.action_mapFragment_to_add_seller_nav_graph)
            }

            fabManageSellers.setOnClickListener {
                navController.navigate(R.id.action_mapFragment_to_manage_seller_nav_graph)
            }

        }

    }


    /**
     *
     * Map setup
     *
     * */
// Initialize the map
    private fun initMap() {

        // Create a custom icon for the marker
        val icon: BitmapDescriptor by lazy {
            val color = ContextCompat.getColor(
                requireContext(),
                R.color.accent_dark_green
            )

            BitmapHelper.vectorToBitmapDescriptor(
                requireContext(),
                R.drawable.ic_durian_24,
                color // color of the icon
            )
        }

        val mapFragment = binding.mapFragment.getFragment() as SupportMapFragment

        viewLifecycleOwner.lifecycleScope.launch {
            val googleMap = mapFragment.awaitMap()

            // Wait for map to finish loading
            googleMap.awaitMapLoad()

            googleMapManager = GoogleMapManager(requireContext(), googleMap)

            viewModel.sellers
                .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
                .distinctUntilChangedBy { it }
                .mapLatest { places ->
                    googleMapManager.initClusteredMarkers(
                        places,
                        icon,
                        MarkerInfoWindowAdapter(requireContext())
                    )
                }
                .launchIn(viewLifecycleOwner.lifecycleScope)


            // Handle place / item on map selected
            googleMapManager.setOnMarkerSelectedListener { seller ->
//                val userComment = viewModel.getUserComment(seller)
//                val allComments = viewModel.getAllComments(seller)
                viewModel.selectSeller(seller.sellerId)
                openBottomSheetDialog()
            }

            // move to user location
            googleMapManager.getUserLocation { location ->
                val latLng = LatLng(location.latitude, location.longitude)
                googleMapManager.moveToLocation(latLng)
            }

            binding.fabLocateMyself.setOnClickListener {
                // move to user location when clicked
                googleMapManager.getUserLocation { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    googleMapManager.moveToLocation(latLng)
                }
            }
        }
    }


    /**
     *
     * Bottom Sheet Dialog setup
     * */

    private var bottomSheetDialog: BottomSheetDialog? = null
    private var bottomSheetBinding: BottomSheetSellerBinding? = null
    private val userCommentAdapter by lazy { SellerCommentsAdapter(requireContext()) }

    @SuppressLint("SetTextI18n")
    private fun openBottomSheetDialog() {
        bottomSheetDialog?.show()
    }

    private fun updateBottomSheetDialogContent(seller: Seller) {
        Log.e(TAG, "updateBottomSheetDialogContent")

        Log.e(TAG, "seller: $seller")

        bottomSheetBinding!!.apply {

            Common.loadServerImageIntoView(requireContext(), seller.imagePath, imageViewSeller)

            textViewSellerName.text = seller.name // set seller name

            // setup chip group
            chipGroupDurianTypes.removeAllViews()
            seller.durianTypes.forEach { durianType ->
                val chip = layoutInflater.inflate(R.layout.chip_durian_type, chipGroupDurianTypes, false) as Chip
                chip.text = durianType.name
                chip.isClickable = false
                chip.isChecked = true
                chipGroupDurianTypes.addView(chip)
            }

            textViewDescriptions.text = seller.description // set seller description

            ratingBarOverallRating.rating = seller.avgRating // set seller rating

            textOverallRating.text = "(%.2f)".format(seller.avgRating) // set seller rating

            // open google map when clicked
            btnSeeInGoogleMap.setOnClickListener {
                // open google map
                GoogleMapManager.openGoogleMap(
                    requireContext(),
                    seller.latLng.latitude,
                    seller.latLng.longitude,
                    seller.name
                )
            }
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setupBottomSheetDialog() {

        bottomSheetDialog = BottomSheetDialog(requireContext())

        bottomSheetBinding = BottomSheetSellerBinding.inflate(layoutInflater)

        bottomSheetBinding!!.recyclerViewAllComments.apply {
            adapter = userCommentAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        bottomSheetDialog!!.apply {
            setContentView(bottomSheetBinding!!.root)
        }


        Log.e(TAG, "setupBottomSheetDialog")
        // observe the states

        // show or hide progress indicator
        viewModel.bottomSheetProgress
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .distinctUntilChangedBy { it }
            .mapLatest { progressing ->
                Log.e(TAG, "progressing: $progressing")
                if (progressing)
                    bottomSheetBinding!!.apply {
                        bottomSheetProgressIndicator.visibility = View.VISIBLE
                        contentContainer.visibility = View.GONE
                    }
                else
                    bottomSheetBinding!!.apply {
                        bottomSheetProgressIndicator.visibility = View.GONE
                        contentContainer.visibility = View.VISIBLE
                    }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

        Log.e(TAG, "observe comments")
        // set the recyclerView adapter
        viewModel.sellerComments
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .distinctUntilChangedBy { it }
            .mapLatest { comments ->
                userCommentAdapter.submitList(comments)
                bottomSheetBinding!!.textAllCommentsTitle.text = "Comments (${comments.size})"
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)


        viewModel.selectedSeller
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .onEach { seller ->
                if (seller == null) return@onEach
                updateBottomSheetDialogContent(seller)
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)

    }

    private fun createUserNotCommentedView() {
        bottomSheetBinding?.apply {
            // clear user comment section
            userCommentSection.removeAllViews()

            // user comment section (check user is added comment or not, then show different view)
            textUserCommentTitle.text = "Add Your Comment"

            val userCommentViewBinding = ItemUserNotCommentedBinding.inflate(
                layoutInflater,
                userCommentSection,
                false
            )

            userCommentViewBinding.apply {
                // TODO : Change to current user image
                Common.loadServerImageIntoView(
                    requireContext(),
                    "",
                    imageUser
                )


                this.commentSection.setOnClickListener {

                    bottomSheetDialog?.dismiss()

                    showAddCommentFragment()

//                    if (navController.currentDestination?.id == R.id.mapFragment)
//                        navController.navigate(R.id.action_mapFragment_to_addCommentFragment)
                }
            }


            userCommentSection.addView(userCommentViewBinding.root)
        }
    }

    private fun createUserCommentedView(userComment: Comment) {
        bottomSheetBinding?.apply {

            // clear user comment section
            userCommentSection.removeAllViews()

            // user comment section (check user is added comment or not, then show different view)
            textUserCommentTitle.text = "Your Comment"

            val userCommentViewBinding = ItemUserCommentedBinding.inflate(
                layoutInflater,
                userCommentSection,
                false
            )

            userCommentViewBinding.apply {
                this.textComment.text = userComment.content
                this.ratingUser.rating = userComment.rating
                this.textUsername.text = userComment.username

                // TODO : Change to current user image
                Common.loadServerImageIntoView(
                    requireContext(),
                    userComment.userImage,
                    imageUser
                )
            }

            userCommentSection.addView(userCommentViewBinding.root)
        }
    }


    /**
     *
     * Add Comment Fragment
     *
     * */
    private val addCommentFragment = AddCommentFragment()
    private fun showAddCommentFragment() {
        // Check if the fragment is already added
        if (childFragmentManager.findFragmentById(binding.addCommentFragmnet.id) == null) {
            childFragmentManager.beginTransaction()
                .add(binding.addCommentFragmnet.id, addCommentFragment)
                .addToBackStack(null) // Adds fragment to the back stack
                .commit()

            addCommentFragment.setOnBackClickListener {
                openBottomSheetDialog()
            }

            addCommentFragment.setOnDoneClickListener { comment, rating ->
                viewModel.addComment(comment, rating)
                openBottomSheetDialog()
            }
        }
    }


    companion object {
        const val TAG = "MapFragment"
    }

}
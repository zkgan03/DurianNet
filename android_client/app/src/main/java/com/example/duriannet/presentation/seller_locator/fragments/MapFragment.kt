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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.duriannet.R
import com.example.duriannet.databinding.BottomSheetSellerBinding
import com.example.duriannet.databinding.FragmentMapBinding
import com.example.duriannet.databinding.ItemUserCommentedBinding
import com.example.duriannet.utils.BitmapHelper
import com.example.duriannet.models.Seller
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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChangedBy
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
    private lateinit var searchFragment: SearchFragment
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationAccessChecker = LocationAccessChecker(requireActivity())

        searchFragment = binding.searchFragmentContainer.getFragment() as SearchFragment
        //invoke only searchFragment in onViewCreated
        searchFragment.setOnViewCreatedListener {
            // init search view
            onSearchFragmentViewCreated()
        }

        //
        //observe the states
        setupStateObservers()

        initMap()

        // FABs setup
        setupFAB()

    }

    override fun onResume() {
        super.onResume()

        isGpsSettingOpened = false // when activity is resumed, the setting must be closed

        // Request permissions
        if (!Common.hasPermissions(requireContext(), PERMISSIONS_REQUIRED)) {
            requestAllPermissionLauncher.launch(PERMISSIONS_REQUIRED)
        } else {

            lifecycleScope.launch {
                // observe GPS status after resumed (to get permission on location)
                locationAccessChecker.observeGpsStatus().collectLatest { isGpsEnabled ->
                    if (!isGpsEnabled) {
                        requestLocationAccess()
                    } else {
                        isGpsSettingOpened = false
                    }
                }

            }
        }
    }


    private fun onSearchFragmentViewCreated() {
        searchFragment.apply {
            setSearchViewAdapter(adapter)

            setSearchViewLayoutManager(LinearLayoutManager(requireContext()))

            setSearchHint("Search for a seller") // set search hint

            setOnSearchViewInputListener { input ->
                viewModel.updateQueryAndSearch(input)
            }

            setOnSearchViewActionListener { query ->
                //            viewModel.updateQueryAndSearch(query)
            }
        }

        //
        // SearchView setup
        // search results adapter
        adapter.setOnItemClickedListener { seller ->
            // item clicked
            Toast.makeText(requireContext(), "Item clicked: ${seller.name}", Toast.LENGTH_SHORT).show()

            viewModel.updateQuery(seller.name) // update query value
            searchFragment.hideSearchView() // hide search view
            googleMapManager.moveToLocation(seller.latLng) // move to location
            googleMapManager.onSelect(seller) // select place
        }
    }

    private fun setupStateObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    // collect search results
                    viewModel.searchResults
                        .distinctUntilChangedBy { it }
                        .collectLatest { searchResults ->
                            Log.e(TAG, "collect search results: $searchResults")
                            adapter.submitList(searchResults)
                        }
                }

                launch {
                    // collect query
                    viewModel.query
                        .distinctUntilChangedBy { it }
                        .collectLatest { query ->
                            Log.e(TAG, "collect query: $query")
                            searchFragment.setSearchBarText(query)
                        }
                }

                launch {
                    // collect sellers
                    viewModel.sellers
                        .distinctUntilChangedBy { it }
                        .collectLatest { sellers ->
                            adapter.submitList(
                                sellers
                            )
                        }
                }


            }
        }
    }

    private fun setupFAB() {
        binding.apply {
            fabAddSeller.setOnClickListener {
                // TODO : Navigate to Add Seller fragment
                navController.navigate(R.id.action_mapFragment_to_add_seller_nav_graph)
            }

            fabManageSellers.setOnClickListener {
                // TODO : Navigate to Manage Sellers fragment
                navController.navigate(R.id.action_mapFragment_to_manage_seller_nav_graph)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        bottomSheetDialog = null
        bottomSheetBinding = null
    }


    /**
     *
     * Map setup
     * */

    // Create a custom icon for the marker
    private val icon: BitmapDescriptor by lazy {
        val color = ContextCompat.getColor(
            requireContext(),
            R.color.cta_color
        )

        BitmapHelper.vectorToBitmapDescriptor(
            requireContext(),
            R.drawable.ic_durian_24,
            color // color of the icon
        )
    }


    // Initialize the map
    private fun initMap() {
        val mapFragment = binding.mapFragment.getFragment() as SupportMapFragment

        viewLifecycleOwner.lifecycleScope.launch {
            val googleMap = mapFragment.awaitMap()

            // Wait for map to finish loading
            googleMap.awaitMapLoad()

            googleMapManager = GoogleMapManager(requireContext(), googleMap)

            launch {
                viewModel.sellers
                    .distinctUntilChangedBy { it }
                    .collectLatest { places ->
                        googleMapManager.initClusteredMarkers(
                            places,
                            icon,
                            MarkerInfoWindowAdapter(requireContext())
                        )
                    }
            }

            // Handle place / item on map selected
            googleMapManager.setOnMarkerSelectedListener { seller ->
//                val userComment = viewModel.getUserComment(seller)
//                val allComments = viewModel.getAllComments(seller)
                openBottomSheetDialog(seller)
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
    private val userCommentAdapter by lazy { SellerCommentsAdapter() }

    @SuppressLint("SetTextI18n")
    private fun openBottomSheetDialog(seller: Seller) {

        if (bottomSheetDialog == null) setupBottomSheetDialog()

        updateBottomSheetDialogContent(seller)

        bottomSheetDialog?.show()
    }

    private fun updateBottomSheetDialogContent(seller: Seller) {
        Log.e(TAG, "updateBottomSheetDialogContent")

        Log.e(TAG, "seller: $seller")

        viewModel.getSellerComments(seller.sellerId) // get seller comments

        bottomSheetBinding!!.apply {

            imageViewSeller.setImageBitmap(seller.image) // set seller image

            textViewSellerName.text = seller.name // set seller name

            // setup chip group
//            chipGroupDurianTypes.removeAllViews() // clear all chips (since there is some chips already added previously)

            seller.durianTypes.forEach { durianType ->
                val chip = layoutInflater.inflate(R.layout.chip_durian_type, chipGroupDurianTypes, false) as Chip
                chip.text = durianType.name
                chip.isClickable = false
                chip.isChecked = true
                chipGroupDurianTypes.addView(chip)
            }

            textViewDescriptions.text = seller.description // set seller description

            ratingBarOverallRating.rating = seller.rating // set seller rating

            textOverallRating.text = "(%.2f)".format(seller.rating) // set seller rating

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

            // get user comment by seller

//                textUserAddedCommentTitle.visibility = if (item.userComment.isEmpty()) View.VISIBLE else View.GONE
//                textUserNotAddedCommentTitle.visibility = if (item.userComment.isEmpty()) View.VISIBLE else View.GONE

            // user comment section (check user is added comment or not, then show different view)
            textUserAddedCommentTitle.visibility = View.GONE
            textUserNotAddedCommentTitle.visibility = View.VISIBLE

            val userCommentViewBinding = ItemUserCommentedBinding.inflate(
                layoutInflater,
                userCommentSection,
                false
            )

            userCommentViewBinding.apply {
                this.iconEdit.visibility = View.VISIBLE
                this.iconDelete.visibility = View.VISIBLE
                this.textComment.text = "This is a user comment"
                this.ratingUser.rating = 4.5f
                this.textUsername.text = "John Doe"
                this.imageUser.setImageBitmap(seller.image)
            }

            userCommentSection.addView(userCommentViewBinding.root)
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
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {

                launch {
                    // show or hide progress indicator
                    viewModel.bottomSheetProgress
                        .distinctUntilChangedBy { it }
                        .collectLatest { progressing ->
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
                }

                launch {
                    Log.e(TAG, "observe comments")
                    // set the recyclerView adapter
                    viewModel.sellerComments
                        .distinctUntilChangedBy { it }
                        .collectLatest { comments ->
                            userCommentAdapter.submitList(comments)
                            bottomSheetBinding!!.textAllCommentsTitle.text = "Comments (${comments.size})"

                        }
                }
            }
        }

    }


    companion object {
        const val TAG = "MapFragment"
    }

}
package com.example.duriannet.presentation.seller_locator.fragments.map_locator

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
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
import com.example.duriannet.models.Comment
import com.example.duriannet.models.Seller
import com.example.duriannet.models.asSeller
import com.example.duriannet.presentation.seller_locator.adapter.MarkerInfoWindowAdapter
import com.example.duriannet.presentation.seller_locator.adapter.SearchResultsAdapter
import com.example.duriannet.presentation.seller_locator.adapter.SellerCommentsAdapter
import com.example.duriannet.presentation.seller_locator.events.MapEvent
import com.example.duriannet.presentation.seller_locator.state.CommentData
import com.example.duriannet.presentation.seller_locator.state.MapScreenState
import com.example.duriannet.presentation.seller_locator.view_models.MapViewModel
import com.example.duriannet.services.common.GoogleMapManager
import com.example.duriannet.services.common.LocationAccessChecker
import com.example.duriannet.utils.BitmapHelper
import com.example.duriannet.utils.Common
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.LatLng
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.snackbar.Snackbar
import com.google.maps.android.ktx.awaitMap
import com.google.maps.android.ktx.awaitMapLoad
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged
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
            }
        }

    // Location access checker
    private lateinit var locationAccessChecker: LocationAccessChecker

    // Google Maps
    private var googleMapManager: GoogleMapManager<Seller>? = null

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

    private val adapter by lazy {
        SearchResultsAdapter()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        locationAccessChecker = LocationAccessChecker(requireActivity())

        //
        //observe the states
        setupUI()
        setupStateObservers()
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

        viewModel.onEvent(MapEvent.RefreshSellers) // refresh sellers

        // Request permissions
        if (!Common.hasPermissions(requireContext(), PERMISSIONS_REQUIRED)) {
            requestAllPermissionLauncher.launch(PERMISSIONS_REQUIRED)
        } else {

            initMap()

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

            viewModel.initViewModel()
        }
    }


    private fun setupStateObservers() {

        viewModel.state
            .flowWithLifecycle(viewLifecycleOwner.lifecycle, Lifecycle.State.STARTED)
            .mapLatest { state ->
                Log.e(TAG, "state changed : $state")
                when (state) {
                    is MapScreenState.Loading -> showLoading()
                    is MapScreenState.Success -> handleSuccessState(state)
                    is MapScreenState.Error -> handleError(state.message)
                }
            }.launchIn(viewLifecycleOwner.lifecycleScope)

    }

    private fun showLoading() {
        binding.progress.visibility = View.VISIBLE
        Toast.makeText(requireContext(), "Loading", Toast.LENGTH_SHORT).show()
    }

    private fun handleSuccessState(state: MapScreenState.Success) {
        Log.e(TAG, "handleSuccessState")
        binding.progress.visibility = View.GONE
        updateSearchState(state)
        updateBottonSheerState(state)
        updateGoogleMapState(state)
    }

    private fun updateSearchState(state: MapScreenState.Success) {
        Log.e(TAG, "search state : ${state.searchResults}")
        adapter.submitList(state.searchResults)
        binding.searchBar.setText(state.query)
    }

    private fun updateBottonSheerState(state: MapScreenState.Success) {
        state.userComment?.let {
            createUserCommentedView(it)
        } ?: createUserNotCommentedView()

        bottomSheetBinding?.apply {
            if (state.isBottomSheetLoading) {
                bottomSheetProgressIndicator.visibility = View.VISIBLE
                contentContainer.visibility = View.GONE
            } else {
                bottomSheetProgressIndicator.visibility = View.GONE
                contentContainer.visibility = View.VISIBLE
            }
            userCommentAdapter.submitList(state.sellerComments)
            textAllCommentsTitle.text = "Comments (${state.sellerComments.size})"
        }

        state.selectedSeller?.let {
            updateBottomSheetDialogContent(it)
        }
    }

    private fun updateGoogleMapState(state: MapScreenState.Success) {
        Log.e(TAG, "updateGoogleMapState : ${state.sellers}")
        googleMapManager?.updataAllItems(
            state.sellers,
        )
    }

    private fun handleError(message: String) {
        binding.progress.visibility = View.GONE
        // show snackbar with retry option
        Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE)
            .setAction("Retry") {
                Log.e(TAG, "Retry loading")
                viewModel.onEvent(MapEvent.RetryLoading)
            }
            .show()
    }


    private fun setupUI() {

        // SearchView setup
        // search results adapter
        adapter.setOnItemClickedListener { sellerSearchResult ->
            // item clicked
            viewModel.onEvent(MapEvent.SelectSeller(sellerSearchResult.sellerId)) // select seller
            viewModel.onEvent(MapEvent.UpdateQuery(sellerSearchResult.name)) // update query value

//            viewModel.updateQuery(seller.name) // update query value

            binding.searchView.hide() // hide search view
            googleMapManager?.apply {
                moveToLocation(LatLng(sellerSearchResult.latitude, sellerSearchResult.longitude)) // move to location
                onSelect(sellerSearchResult.asSeller()) // select place
            }

        }

        binding.apply {
//            GoogleMapManager.getUserLocation(requireContext()) { location ->
//                adapter.updateUserLocation(Pair(location.latitude, location.longitude))
//            }

            recyclerResults.adapter = adapter
            recyclerResults.layoutManager = LinearLayoutManager(requireContext())

            searchBar.hint = "Search for a seller"
            searchView.hint = "Search for a seller"

            searchView.editText.doOnTextChanged { text, start, before, count ->
                searchBar.setText(text.toString())
                viewModel.onEvent(MapEvent.UpdateQuery(text.toString()))
//                viewModel.handleQueryUpdate(text.toString())
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

    // Create a custom icon for the marker
    private val icon: BitmapDescriptor by lazy {
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

    // Initialize the map
    private fun initMap() {
        if (googleMapManager != null) return

        val mapFragment = binding.mapFragment.getFragment() as SupportMapFragment

        viewLifecycleOwner.lifecycleScope.launch {
            val googleMap = mapFragment.awaitMap()

            // Wait for map to finish loading
            googleMap.awaitMapLoad()

            googleMapManager = GoogleMapManager<Seller>(requireContext(), googleMap).apply {


                // Handle place / item on map selected
                setOnMarkerSelectedListener { seller ->
//                viewModel.handleSellerSelection(seller.sellerId)
                    viewModel.onEvent(MapEvent.SelectSeller(seller.sellerId))
                    openBottomSheetDialog()
                }

                // move to user location
                getUserLocation { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    moveToLocation(latLng)
                }

                initClusteredMarkers(
                    (viewModel.state.value as? MapScreenState.Success)?.sellers ?: emptyList(),
                    icon,
                    MarkerInfoWindowAdapter(requireContext())
                )

                binding.fabLocateMyself.setOnClickListener {
                    // move to user location when clicked
                    getUserLocation { location ->
                        val latLng = LatLng(location.latitude, location.longitude)
                        moveToLocation(latLng)
                    }
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

                    showACommentFragment()

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

            userCommentViewBinding.iconEdit.setOnClickListener {
                showACommentFragment(
                    CommentData(
                        content = userComment.content,
                        rating = userComment.rating,
                        isEdit = true
                    )
                )

                bottomSheetDialog?.dismiss()
            }

            userCommentViewBinding.iconDelete.setOnClickListener {
                val builder = AlertDialog.Builder(requireContext())
                builder.setMessage("Are you confirm to delete this comment?")
                    .setCancelable(false)
                    .setPositiveButton("Delete") { dialog, id ->
                        viewModel.onEvent(MapEvent.DeleteComment)

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

            userCommentSection.addView(userCommentViewBinding.root)
        }
    }


    /**
     *
     * Add Comment Fragment
     *
     * */
    private fun showACommentFragment(existingComment: CommentData? = null) {
        val commentChildFragment = CommentChildFragment.newInstance(existingComment)
        // Check if the fragment is already added
        if (childFragmentManager.findFragmentById(binding.addCommentFragmnet.id) == null) {
            childFragmentManager.beginTransaction()
                .add(binding.addCommentFragmnet.id, commentChildFragment)
                .addToBackStack(null) // Adds fragment to the back stack
                .commit()

            commentChildFragment.setOnBackClickListener {
                openBottomSheetDialog()
            }

            commentChildFragment.setOnDoneClickListener { comment, rating ->
                if (existingComment != null) {
                    viewModel.onEvent(MapEvent.EditComment(comment, rating))
                } else {
                    viewModel.onEvent(MapEvent.AddComment(comment, rating))
                }
                openBottomSheetDialog()
            }
        }
    }


    companion object {
        const val TAG = "MapFragment"
    }

}
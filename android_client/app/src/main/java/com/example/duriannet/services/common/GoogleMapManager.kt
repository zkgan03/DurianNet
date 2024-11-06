package com.example.duriannet.services.common

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.net.Uri
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.duriannet.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import com.google.maps.android.ktx.addCircle

class GoogleMapManager<T : ClusterItem>(
    val context: Context,
    val googleMap: GoogleMap,
) {

    private val fusedLocationClient: FusedLocationProviderClient =
        LocationServices.getFusedLocationProviderClient(context)

    init {
        setupMap()
    }

    private fun setupMap() {

        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        // Disable the map toolbar
        // refs : https://developers.google.com/maps/documentation/android-sdk/controls
        googleMap.uiSettings.apply {
            isMapToolbarEnabled = false
            isMyLocationButtonEnabled = false
        }

        googleMap.isMyLocationEnabled = true

    }

    private var onMarkerSelected: ((T) -> Unit)? = null
    private val clusterManager = ClusterManager<T>(context, googleMap)

    /**
     *  Initialize the clustered markers on the map
     *  NOTE : only call this method once
     * */
    fun initClusteredMarkers(
        places: List<T>? = null,
        icon: BitmapDescriptor,
        infoWindowAdapter: GoogleMap.InfoWindowAdapter,
    ) {

        clusterManager.apply {

            renderer = PlaceRenderer(
                context = context,
                map = googleMap,
                icon = icon,
                clusterManager = this
            )

            markerCollection.setInfoWindowAdapter(infoWindowAdapter)

            addItems(places)
            cluster() // re-cluster / re-render

            setOnClusterItemClickListener { item ->
                addCircleAroundPlace(item.position)
                onMarkerSelected?.invoke(item)
                false
            }
        }

        // When the camera starts moving, change the alpha value of the marker to translucent
        googleMap.setOnCameraMoveStartedListener()
        {
            val alpha = 0.3f
            clusterManager.markerCollection?.markers?.forEach { it.alpha = alpha }
            clusterManager.clusterMarkerCollection?.markers?.forEach { it.alpha = alpha }
        }

        // When the camera stops moving, change the alpha value back to opaque
        googleMap.setOnCameraIdleListener()
        {
            val alpha = 1.0f
            clusterManager.markerCollection?.markers?.forEach { it.alpha = alpha }
            clusterManager.clusterMarkerCollection?.markers?.forEach { it.alpha = alpha }

            // Call clusterManager.onCameraIdle() when the camera stops moving so that re-clustering
            // can be performed when the camera stops moving
            clusterManager.onCameraIdle()
        }

    }


    fun setOnMarkerSelectedListener(onMarkerSelected: (T) -> Unit) {
        this.onMarkerSelected = onMarkerSelected
    }

    fun clearMarkers() {
        clusterManager.clearItems()
        clusterManager.cluster()
    }


    fun moveToLocation(latLng: LatLng, zoomLevel: Float = 15f) {
        googleMap.animateCamera(
            CameraUpdateFactory.newLatLngZoom(latLng, zoomLevel)
        )
    }

    fun onSelect(place: T) {
        clusterManager.markerCollection.markers.forEach {
            if (it.position == place.position) {
                it.showInfoWindow()
            }
        }
        addCircleAroundPlace(place.position)
        onMarkerSelected?.invoke(place)
    }

    fun updateItem(place: T) {
        clusterManager.updateItem(place)
        clusterManager.cluster()
    }

    fun getItemsAdded(): List<T> {
        return clusterManager.algorithm.items.toList()
    }

    fun getItemsFromLocation(location: LatLng): List<T> {
        return clusterManager.algorithm.items.filter { it.position == location }
    }

    fun setItems(places: List<T>) {
        clusterManager.clearItems()
        clusterManager.addItems(places)
        clusterManager.cluster()
    }

    /**
     *  Returns the bounds of the places
     *
     * */
    fun getBoundsFromItems(places: List<T>): LatLngBounds {
        val bounds = LatLngBounds.builder()
        places.forEach { bounds.include(it.position) }

        return bounds.build()
    }


    fun getUserLocation(onLocationReceived: (Location) -> Unit) {
        if (
            ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }


        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                onLocationReceived(it)
            }
        }
    }


    private var circle: Circle? = null
    private fun addCircleAroundPlace(location: LatLng) {
        circle?.remove()
        circle = googleMap.addCircle {
            center(location)
            radius(1000.0)
            fillColor(ContextCompat.getColor(context, R.color.active_indicator_green_translucent))
            strokeColor(ContextCompat.getColor(context, R.color.accent_dark_green))
        }
    }


    /**
     * Custom renderer for the cluster items
     */
    inner class PlaceRenderer(
        context: Context,
        map: GoogleMap,
        private val icon: BitmapDescriptor,
        clusterManager: ClusterManager<T>,
    ) : DefaultClusterRenderer<T>(context, map, clusterManager) {
        /**
         * The icon to use for each cluster item
         */
//        private val icon: BitmapDescriptor by lazy {
//            val color = ContextCompat.getColor(
//                context,
//                R.color.primary_color
//            )
//
//            BitmapHelper.vectorToBitmapDescriptor(
//                context,
//                R.drawable.ic_directions_bike_black_24dp,
//                color // color of the icon
//            )
//        }

        /**
         * Method called before the cluster item (i.e. the marker) is rendered. This is where marker
         * options should be set
         */
        override fun onBeforeClusterItemRendered(item: T, markerOptions: MarkerOptions) {
            markerOptions.title(item.title)
                .position(item.position)
                .icon(icon)
        }

        /**
         * Method called right after the cluster item (i.e. the marker) is rendered. This is where
         * properties for the Marker object should be set.
         */
        override fun onClusterItemRendered(clusterItem: T, marker: Marker) {
            marker.tag = clusterItem
        }
    }

    companion object {
        private const val TAG = "GoogleMapManager"

        fun getUserLocation(context: Context, onLocationReceived: (Location) -> Unit) {
            val fusedLocationClient: FusedLocationProviderClient =
                LocationServices.getFusedLocationProviderClient(context)

            if (
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    onLocationReceived(it)
                }
            }
        }


        fun getAddress(context: Context, lat: Double, lon: Double): String {

            if (!Geocoder.isPresent()) {
                return "Geocoder not present"
            }

            val geocoder = Geocoder(context)

            val addresses = geocoder.getFromLocation(lat, lon, 1)?.get(0)

            val sb = StringBuilder()
            if (addresses != null) {
                for (i in 0..addresses.maxAddressLineIndex) {
                    Log.e("Utils", "getAddress: ${addresses.getAddressLine(i)}")
                    sb.append(addresses.getAddressLine(i) + "\n")
                }
            }

            return sb.toString()
        }

        fun openGoogleMap(context: Context, latitude: Double, longitude: Double, locationName: String = "Durian Seller") {
            val uri = Uri.parse("geo:$latitude,$longitude?q=$latitude,$longitude($locationName)")
            val intent = Intent(Intent.ACTION_VIEW, uri)

            // Specify Google Maps as the app to handle the Intent
            intent.setPackage("com.google.android.apps.maps")

            // Check if Google Maps is installed on the device
            if (intent.resolveActivity(context.packageManager) != null) {
                startActivity(context, intent, null)
            } else {
                // If Google Maps is not installed, open the browser
                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse("http://maps.google.com/maps?q=loc:$latitude,$longitude"))
                startActivity(context, browserIntent, null)
            }
        }
    }

}
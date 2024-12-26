package com.example.duriannet.models

import com.example.duriannet.services.common.GoogleMapManager
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

data class Seller(
    val sellerId: Int,
    val name: String,
    val description: String,
    val durianTypes: HashSet<DurianType>,
    val imagePath: String,
    val avgRating: Float,
    val latLng: LatLng,
    val userId: String,
    val username: String,
) : ClusterItem {
    override fun getPosition(): LatLng = latLng
    override fun getTitle(): String = name
    override fun getSnippet(): String = description
    override fun getZIndex(): Float = avgRating
}

fun Seller.toSellerSearchResult(
    userLatLng: Pair<Double, Double>,
): SellerSearchResult = SellerSearchResult(
    sellerId = sellerId,
    name = name,
    latitude = latLng.latitude,
    longitude = latLng.longitude,
    distanceFromCurrentLocationInKm = if (userLatLng.first != 0.0 && userLatLng.second != 0.0) {
        GoogleMapManager.calculateDistanceInMeter(
            userLatLng.first,
            userLatLng.second,
            latLng.latitude,
            latLng.longitude,
        ) / 1000
    } else {
        0f
    },
)

enum class DurianType {
    MusangKing,
    D24,
    RedPrawn,
    BlackThorn,
}
package com.example.duriannet.models

import com.google.android.gms.maps.model.LatLng

data class SellerSearchResult(
    val sellerId: Int,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    val distanceFromCurrentLocationInKm: Float,
)

fun SellerSearchResult.asSeller(): Seller = Seller(
    sellerId = sellerId,
    name = name,
    description = "",
    durianTypes = hashSetOf(),
    imagePath = "",
    avgRating = 0f,
    latLng = LatLng(latitude, longitude),
    userId = "",
    username = "",
)
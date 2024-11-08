package com.example.duriannet.data.remote.dtos.response

import com.example.duriannet.models.DurianType
import com.example.duriannet.models.Seller
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class SellerResponse(
    @SerializedName("sellerId") val sellerId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("imageUrl") val imageUrl: String,
    @SerializedName("rating") val rating: Float,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("durianTypes") val durianTypes: List<DurianTypeResponse>,
    @SerializedName("addByUser") val user: SellerUserResponse,
)

data class DurianTypeResponse(
    @SerializedName("durianId") val durianId: Int,
    @SerializedName("name") val name: String,
)

data class SellerUserResponse(
    @SerializedName("userId") val userId: String,
    @SerializedName("username") val username: String,
)


fun SellerResponse.toSeller(): Seller = Seller(
    sellerId = sellerId,
    name = name,
    description = description,
    durianTypes = durianTypes.map { DurianType.valueOf(it.name.replace(" ", "")) }.toHashSet(),
    imagePath = imageUrl,
    avgRating = rating,
    latLng = LatLng(latitude, longitude),
    userId = user.userId,
    username = user.username,
)
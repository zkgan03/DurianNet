package com.example.duriannet.data.remote.dtos

import com.example.duriannet.models.DurianType
import com.example.duriannet.models.Seller
import com.example.duriannet.utils.BitmapHelper
import com.google.android.gms.maps.model.LatLng
import com.google.gson.annotations.SerializedName

data class GetSellerResponse(
    @SerializedName("sellerId") val sellerId: Int,
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("durianTypes") val durianTypes: List<String>,
    @SerializedName("rating") val rating: Float,
    @SerializedName("image") val image: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
)


fun GetSellerResponse.toSeller(): Seller = Seller(
    sellerId = sellerId,
    name = name,
    description = description,
    durianTypes = durianTypes.map { DurianType.valueOf(it.replace(" ", "")) }.toHashSet(),
    image = BitmapHelper.decodeBase64Image(image),
    rating = rating,
    latLng = LatLng(latitude, longitude),
)
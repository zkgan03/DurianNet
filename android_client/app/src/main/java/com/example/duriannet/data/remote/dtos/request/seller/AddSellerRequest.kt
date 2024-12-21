package com.example.duriannet.data.remote.dtos.request.seller

import com.example.duriannet.models.Seller
import com.example.duriannet.utils.BitmapHelper
import com.google.gson.annotations.SerializedName

data class AddSellerRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("base64Image") val image: String,
    @SerializedName("latitude") val latitude: Double,
    @SerializedName("longitude") val longitude: Double,
    @SerializedName("durianProfileId") val durianProfileId: List<Int> = emptyList(),
)


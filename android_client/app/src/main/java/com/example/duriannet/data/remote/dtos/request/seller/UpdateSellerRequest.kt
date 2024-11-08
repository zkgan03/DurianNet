package com.example.duriannet.data.remote.dtos.request.seller

import com.example.duriannet.models.Seller
import com.google.gson.annotations.SerializedName

data class UpdateSellerRequest(
    @SerializedName("name") val name: String,
    @SerializedName("description") val description: String,
    @SerializedName("durianProfileId") val durianProfileId: List<Int> = emptyList(),
)

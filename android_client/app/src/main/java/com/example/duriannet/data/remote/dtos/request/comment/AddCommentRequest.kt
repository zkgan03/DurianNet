package com.example.duriannet.data.remote.dtos.request.comment

import com.example.duriannet.models.Comment
import com.google.gson.annotations.SerializedName

data class AddCommentRequest(
    @SerializedName("sellerId") val sellerId: Int,
    @SerializedName("rating") val rating: Float,
    @SerializedName("content") val content: String,
)
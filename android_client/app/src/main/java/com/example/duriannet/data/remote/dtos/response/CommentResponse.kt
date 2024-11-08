package com.example.duriannet.data.remote.dtos.response

import com.example.duriannet.models.Comment
import com.google.gson.annotations.SerializedName

data class CommentResponse(
    @SerializedName("commentId") val commentId: Int,
    @SerializedName("rating") val rating: Float,
    @SerializedName("content") val content: String,
    @SerializedName("user") val user: CommentUserResponse,
    @SerializedName("seller") val seller: CommentSellerResponse,
)

data class CommentUserResponse(
    @SerializedName("userId") val userId: String,
    @SerializedName("username") val username: String,
    @SerializedName("imageUrl") val imageUrl: String?,
)

data class CommentSellerResponse(
    @SerializedName("sellerId") val sellerId: Int,
    @SerializedName("name") val name: String,
)


fun CommentResponse.toSellerComment(): Comment = Comment(
    commentId = commentId,
    rating = rating,
    content = content,
    userId = user.userId,
    username = user.username,
    sellerId = seller.sellerId,
    sellerName = seller.name,
    userImage = user.imageUrl,
)
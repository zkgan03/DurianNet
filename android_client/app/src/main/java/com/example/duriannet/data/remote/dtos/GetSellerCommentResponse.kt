package com.example.duriannet.data.remote.dtos

import com.example.duriannet.models.SellerComment
import com.example.duriannet.models.User
import com.google.gson.annotations.SerializedName

data class GetSellerCommentResponse(
    @SerializedName("commentId") val commentId: Int,
    @SerializedName("rating") val rating: Float,
    @SerializedName("content") val content: String,
    @SerializedName("user") val user: GetUserResponse,
)

data class GetUserResponse(
    @SerializedName("userId") val userId: Int,
    @SerializedName("userName") val username: String,
)

fun GetUserResponse.toUser(): User = User(
    userId = userId,
    username = username,
)

fun GetSellerCommentResponse.toSellerComment(): SellerComment = SellerComment(
    commentId = commentId,
    rating = rating,
    content = content,
    user = user.toUser(),
)
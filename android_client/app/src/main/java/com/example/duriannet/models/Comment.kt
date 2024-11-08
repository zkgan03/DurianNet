package com.example.duriannet.models

data class Comment(
    val commentId: Int,
    val rating: Float,
    val content: String,
    val userId: String,
    val username : String,
    val userImage : String?,
    val sellerId: Int,
    val sellerName: String,
)
package com.example.duriannet.models

data class SellerComment(
    val commentId: Int,
    val rating: Float,
    val content: String,
    val user: User,
)
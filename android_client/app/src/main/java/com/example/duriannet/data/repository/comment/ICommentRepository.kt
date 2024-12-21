package com.example.duriannet.data.repository.comment

import com.example.duriannet.models.Comment

interface ICommentRepository {
    suspend fun getSellerComments(sellerId: Int): Result<List<Comment>>

    suspend fun getCommentById(commentId: Int): Result<Comment>

    suspend fun addComment(
        sellerId: Int,
        rating: Float,
        content: String
    ): Result<Comment>

    suspend fun updateComment(
        commentId: Int,
        rating: Float,
        content: String
    ): Result<Comment>

    suspend fun deleteComment(commentId: Int): Result<Unit>
}
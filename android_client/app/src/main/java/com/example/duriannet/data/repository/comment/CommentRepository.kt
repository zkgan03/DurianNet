package com.example.duriannet.data.repository.comment

import com.example.duriannet.data.remote.api.CommentApi
import com.example.duriannet.data.remote.dtos.request.comment.AddCommentRequest
import com.example.duriannet.data.remote.dtos.request.comment.UpdateCommentRequest
import com.example.duriannet.data.remote.dtos.response.toSellerComment
import com.example.duriannet.models.Comment
import javax.inject.Inject

class CommentRepository @Inject constructor(
    private val commentApi: CommentApi,
) : ICommentRepository {
    override suspend fun getSellerComments(sellerId: Int): Result<List<Comment>> {

        val sellerComments = try {
            commentApi.getSellerCommentsById(sellerId)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!sellerComments.isSuccessful) {
            return Result.failure(Exception("Failed to get seller comments : ${sellerComments.errorBody()}"))
        }

        val comments = sellerComments.body()?.map { it.toSellerComment() }

        return Result.success(comments ?: emptyList())
    }

    override suspend fun getCommentById(commentId: Int): Result<Comment> {

        val comment = try {
            commentApi.getCommentById(commentId)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!comment.isSuccessful) {
            return Result.failure(Exception("Failed to get comment : ${comment.errorBody()}"))
        }

        return Result.success(comment.body()!!.toSellerComment())
    }

    override suspend fun addComment(
        userId: String,
        sellerId: Int,
        rating: Float,
        content: String
    ): Result<Comment> {

        val addCommentRequest = AddCommentRequest(
            userId = userId,
            sellerId = sellerId,
            rating = rating,
            content = content
        )

        val response = try {
            commentApi.addComment(addCommentRequest)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!response.isSuccessful) {
            return Result.failure(Exception("Failed to add comment : ${response.errorBody()}"))
        }


        return Result.success(response.body()!!.toSellerComment())
    }

    override suspend fun updateComment(
        commentId: Int,
        rating: Float,
        content: String,
    ): Result<Comment> {
        val updateCommentRequest = UpdateCommentRequest(
            rating = rating,
            content = content
        )

        val response = try {
            commentApi.updateComment(commentId, updateCommentRequest)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!response.isSuccessful) {
            return Result.failure(Exception("Failed to update comment : ${response.errorBody()}"))
        }

        return Result.success(response.body()!!.toSellerComment())
    }

    override suspend fun deleteComment(commentId: Int): Result<Unit> {
        val response = try {
            commentApi.deleteComment(commentId)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!response.isSuccessful) {
            return Result.failure(Exception("Failed to delete comment : ${response.errorBody()}"))
        }

        return Result.success(Unit)
    }

}
package com.example.duriannet.data.remote.api

import com.example.duriannet.data.remote.dtos.request.comment.AddCommentRequest
import com.example.duriannet.data.remote.dtos.request.comment.UpdateCommentRequest
import com.example.duriannet.data.remote.dtos.response.CommentResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface CommentApi {
    @GET("GetSellerComments/{sellerId}")
    suspend fun getSellerCommentsById(
        @Path("sellerId") sellerId: Int,
    ): Response<List<CommentResponse>>

    @GET("GetComment/{commentId}")
    suspend fun getCommentById(
        @Path("commentId") commentId: Int,
    ): Response<CommentResponse>

    @POST("AddComment")
    suspend fun addComment(
        @Body comment: AddCommentRequest,
    ): Response<CommentResponse>

    @PUT("UpdateComment/{commentId}")
    suspend fun updateComment(
        @Path("commentId") commentId: Int,
        @Body comment: UpdateCommentRequest,
    ): Response<CommentResponse>

    @DELETE("DeleteComment/{commentId}")
    suspend fun deleteComment(
        @Path("commentId") commentId: Int,
    ): Response<Unit>
}
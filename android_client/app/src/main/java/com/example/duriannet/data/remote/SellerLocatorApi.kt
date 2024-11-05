package com.example.duriannet.data.remote

import com.example.duriannet.data.remote.dtos.GetSellerCommentResponse
import com.example.duriannet.data.remote.dtos.GetSellerResponse
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface SellerLocatorApi {
    @GET("SearchSellers")
    suspend fun searchSellers(
        @Query("query") query: String,
    ): Response<List<GetSellerResponse>>

    @GET("GetAllSellers")
    suspend fun getAllSeller(): Response<List<GetSellerResponse>>

    @GET("GetSellerComments/{sellerId}")
    suspend fun getSellerComments(
        @Path("sellerId") sellerId: Int,
    ): Response<List<GetSellerCommentResponse>>
}
package com.example.duriannet.data.remote.api

import com.example.duriannet.data.remote.dtos.request.seller.AddSellerRequest
import com.example.duriannet.data.remote.dtos.request.seller.UpdateSellerRequest
import com.example.duriannet.data.remote.dtos.response.SellerResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface SellerApi {
    @GET("SearchSellers")
    suspend fun searchSellers(
        @Query("query") query: String,
    ): Response<List<SellerResponse>>

    @GET("GetSellers")
    suspend fun getAllSellers(): Response<List<SellerResponse>>

    @GET("GetSellers/{sellerId}")
    suspend fun getSellerById(
        @Path("sellerId") sellerId: Int,
    ): Response<SellerResponse>

    @GET("GetSellersAddedByUser")
    suspend fun getSellersAddedByUser(): Response<List<SellerResponse>>

    @POST("AddSeller")
    suspend fun addSeller(
        @Body seller: AddSellerRequest,
    ): Response<SellerResponse>

    @PUT("UpdateSeller/{sellerId}")
    suspend fun updateSeller(
        @Path("sellerId") sellerId: Int,
        @Body seller: UpdateSellerRequest,
    ): Response<SellerResponse>

    @DELETE("RemoveSeller/{sellerId}")
    suspend fun deleteSeller(
        @Path("sellerId") sellerId: Int,
    ): Response<Unit>
}
package com.example.duriannet.data.repository.seller

import com.example.duriannet.data.remote.dtos.response.SellerResponse
import com.example.duriannet.models.DurianType
import com.example.duriannet.models.Seller
import com.google.gson.annotations.SerializedName

interface ISellerRepository {

    suspend fun searchSellers(query: String): Result<List<Seller>>

    suspend fun getAllSellers(): Result<List<Seller>>

    suspend fun getSellerById(sellerId: Int): Result<Seller>

    suspend fun getSellersAddedByUser(): Result<List<Seller>>

    suspend fun addSeller(
        name: String,
        description: String,
        image: String,
        latitude: Double,
        longitude: Double,
        durianProfileId: List<DurianType> = emptyList(),
    ): Result<Seller>

    suspend fun updateSeller(
        sellerId: Int,
        name: String,
        description: String,
        durianProfileId: List<DurianType> = emptyList(),
    ): Result<Seller>

    suspend fun deleteSeller(sellerId: Int): Result<Unit>
}
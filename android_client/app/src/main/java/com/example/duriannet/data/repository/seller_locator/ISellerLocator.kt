package com.example.duriannet.data.repository.seller_locator

import com.example.duriannet.models.Seller
import com.example.duriannet.models.SellerComment

interface ISellerLocator {

    suspend fun searchSellers(query: String): Result<List<Seller>>

    suspend fun getAllSellers(): Result<List<Seller>>

    suspend fun getSellerComments(sellerId: Int): Result<List<SellerComment>>
}
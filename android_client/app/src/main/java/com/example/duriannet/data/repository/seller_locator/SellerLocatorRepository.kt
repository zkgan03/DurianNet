package com.example.duriannet.data.repository.seller_locator

import com.example.duriannet.data.remote.SellerLocatorApi
import com.example.duriannet.data.remote.dtos.toSeller
import com.example.duriannet.data.remote.dtos.toSellerComment
import com.example.duriannet.models.Seller
import com.example.duriannet.models.SellerComment

import javax.inject.Inject

class SellerLocatorRepository @Inject constructor(
    private val sellerLocatorApi: SellerLocatorApi,
) : ISellerLocator {

    override suspend fun searchSellers(query: String): Result<List<Seller>> {

        val searchResult = try {
            sellerLocatorApi
                .searchSellers(query)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!searchResult.isSuccessful) {
            return Result.failure(Exception("Failed to search seller location"))
        }

        val sellers = searchResult.body()?.map { it.toSeller() }

        return Result.success(sellers ?: emptyList())
    }

    override suspend fun getAllSellers(): Result<List<Seller>> {

        val allSellers = try {
            sellerLocatorApi.getAllSeller()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!allSellers.isSuccessful) {
            return Result.failure(Exception("Failed to get all sellers"))
        }

        val sellers = allSellers.body()?.map { it.toSeller() }

        return Result.success(sellers ?: emptyList())
    }

    override suspend fun getSellerComments(sellerId: Int): Result<List<SellerComment>> {

        val sellerComments = try {
            sellerLocatorApi.getSellerComments(sellerId)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!sellerComments.isSuccessful) {
            return Result.failure(Exception("Failed to get seller comments"))
        }

        val comments = sellerComments.body()?.map { it.toSellerComment() }

        return Result.success(comments ?: emptyList())
    }

}
package com.example.duriannet.data.repository.seller

import android.util.Log
import com.example.duriannet.data.remote.api.SellerApi
import com.example.duriannet.data.remote.dtos.request.seller.AddSellerRequest
import com.example.duriannet.data.remote.dtos.request.seller.UpdateSellerRequest
import com.example.duriannet.data.remote.dtos.response.toSeller
import com.example.duriannet.models.DurianType
import com.example.duriannet.models.Seller

import javax.inject.Inject

class SellerRepository @Inject constructor(
    private val sellerLocatorApi: SellerApi,
) : ISellerRepository {

    override suspend fun searchSellers(query: String): Result<List<Seller>> {

        val searchResult = try {
            sellerLocatorApi
                .searchSellers(query)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!searchResult.isSuccessful) {
            return Result.failure(Exception("Failed to search seller location : ${searchResult.errorBody()}"))
        }

        val sellers = searchResult.body()?.map { it.toSeller() }

        return Result.success(sellers ?: emptyList())
    }

    override suspend fun getAllSellers(): Result<List<Seller>> {

        val allSellers = try {
            sellerLocatorApi.getAllSellers()
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!allSellers.isSuccessful) {
            return Result.failure(Exception("Failed to get all sellers : ${allSellers.errorBody()}"))
        }

        val sellers = allSellers.body()?.map { it.toSeller() }

        return Result.success(sellers ?: emptyList())
    }

    override suspend fun getSellerById(sellerId: Int): Result<Seller> {
        val sellerResult = try {
            sellerLocatorApi.getSellerById(sellerId)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!sellerResult.isSuccessful) {
            return Result.failure(Exception("Failed to get seller by Id : ${sellerResult.errorBody()}"))
        }

        val seller = sellerResult.body()!!.toSeller()

        return Result.success(seller)

    }

    override suspend fun getSellersAddedByUser(userId: String): Result<List<Seller>> {
        val request = try {
            sellerLocatorApi.getSellersAddedByUser(userId)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!request.isSuccessful) {
            return Result.failure(Exception("Failed to get sellers added by user : ${request.errorBody()}"))
        }

        val sellers = request.body()!!.map { it.toSeller() }

        return Result.success(sellers)
    }

    override suspend fun addSeller(
        userId: String,
        name: String,
        description: String,
        base64Image: String,
        latitude: Double,
        longitude: Double,
        durianTypes: List<DurianType>,
    ): Result<Seller> {

        val addSellerRequest = AddSellerRequest(
            userId = userId,
            name = name,
            description = description,
            image = base64Image,
            latitude = latitude,
            longitude = longitude,
            durianProfileId = durianTypes.map { it.ordinal + 1 }
        )

        val response = try {
            sellerLocatorApi.addSeller(addSellerRequest)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!response.isSuccessful) {
            return Result.failure(Exception("Failed to add seller : ${response.errorBody()}"))
        }

        return Result.success(response.body()!!.toSeller())
    }

    override suspend fun updateSeller(
        sellerId: Int,
        name: String,
        description: String,
        durianProfileId: List<DurianType>,
    ): Result<Seller> {

        val request = UpdateSellerRequest(
            name = name,
            description = description,
            durianProfileId = durianProfileId.map { it.ordinal + 1 }
        )

        val response = try {
            Log.e("SellerRepository", "updateSeller: $sellerId, $request")
            sellerLocatorApi.updateSeller(sellerId, request)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!response.isSuccessful) {
            return Result.failure(Exception("Failed to update seller : ${response.errorBody()}"))
        }

        return Result.success(response.body()!!.toSeller())
    }

    override suspend fun deleteSeller(sellerId: Int): Result<Unit> {

        val response = try {
            sellerLocatorApi.deleteSeller(sellerId)
        } catch (e: Exception) {
            return Result.failure(e)
        }

        if (!response.isSuccessful) {
            return Result.failure(Exception("Failed to delete seller : ${response.errorBody()}"))
        }

        return Result.success(Unit)
    }


}
package com.example.duriannet.data.remote.api

import com.example.duriannet.data.remote.dtos.request.durian.AddFavoriteDurianRequestDto
import com.example.duriannet.data.remote.dtos.request.durian.RemoveFavoriteDurianRequestDto
import com.example.duriannet.data.remote.dtos.response.*
import retrofit2.Response
import retrofit2.http.*

interface DurianApi {
    // Get all durians with optional search
    @GET("appApi/durian/appGetAllDurianProfiles")
    suspend fun getAllDurianProfiles(@Query("searchQuery") searchQuery: String? = null): Response<List<DurianProfileForUserResponseDto>>

    // Get favorite durians for a specific user
    @GET("appApi/durian/appGetFavoriteDurians/{username}")
    suspend fun getFavoriteDurians(@Path("username") username: String): Response<List<DurianListResponseDto>>

    // Add a durian to favorites
    @POST("appApi/durian/appAddFavoriteDurian")
    suspend fun addFavoriteDurian(@Body request: AddFavoriteDurianRequestDto): Response<Unit>

    // Remove a durian from favorites
    @DELETE("appApi/durian/appRemoveFavoriteDurian")
    suspend fun removeFavoriteDurian(@Body request: RemoveFavoriteDurianRequestDto): Response<Unit>

    // Get a specific durian profile by ID
    @GET("appApi/durian/appGetDurianProfile/{id}")
    suspend fun getDurianProfileDetails(@Path("id") id: Int): Response<DurianProfileResponseDto>

    // Get all durian profiles for the user (basic details)
    @GET("appApi/durian/appGetAllDurianProfilesForUser")
    suspend fun getAllDurianProfilesForUser(): Response<List<DurianProfileForUserResponseDto>>
}

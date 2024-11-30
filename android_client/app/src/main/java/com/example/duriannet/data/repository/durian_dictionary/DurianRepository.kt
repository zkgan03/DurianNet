package com.example.duriannet.data.repository.durian_dictionary

import com.example.duriannet.data.remote.api.DurianApi
import com.example.duriannet.data.remote.dtos.request.durian.AddFavoriteDurianRequestDto
import com.example.duriannet.data.remote.dtos.request.durian.RemoveFavoriteDurianRequestDto
import com.example.duriannet.data.remote.dtos.response.DurianListResponseDto
import com.example.duriannet.data.remote.dtos.response.DurianProfileForUserResponseDto
import com.example.duriannet.data.remote.dtos.response.DurianProfileResponseDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class DurianRepository @Inject constructor(private val durianApi: DurianApi) {
    suspend fun getAllDurians(): Result<List<DurianProfileForUserResponseDto>> {
        return runCatching {
            val response = durianApi.getAllDurianProfiles()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                throw Exception("Failed to fetch durians: ${response.message()}")
            }
        }
    }

    suspend fun getAllDurianProfilesForUser(): Result<List<DurianProfileForUserResponseDto>> {
        return runCatching {
            val response = durianApi.getAllDurianProfilesForUser()
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                throw Exception("Failed to fetch durian profiles for user: ${response.message()}")
            }
        }
    }

    suspend fun getDurianDetails(id: Int): Result<DurianProfileResponseDto> {
        return runCatching {
            val response = durianApi.getDurianProfileDetails(id)
            if (response.isSuccessful) {
                response.body() ?: throw Exception("No durian profile found")
            } else {
                throw Exception("Failed to fetch durian details: ${response.message()}")
            }
        }
    }


    suspend fun getFavoriteDurians(username: String): Result<List<DurianListResponseDto>> {
        return runCatching {
            val response = durianApi.getFavoriteDurians(username)
            if (response.isSuccessful) {
                response.body() ?: emptyList()
            } else {
                throw Exception("Failed to fetch favorite durians: ${response.message()}")
            }
        }
    }


    suspend fun addFavoriteDurian(username: String, durianName: String) = runCatching {
        durianApi.addFavoriteDurian(AddFavoriteDurianRequestDto(username, durianName))
    }

    suspend fun removeFavoriteDurian(username: String, durianName: String) = runCatching {
        durianApi.removeFavoriteDurian(RemoveFavoriteDurianRequestDto(username, durianName))
    }

}

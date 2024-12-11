package com.example.duriannet.data.repository.account_management

import com.example.duriannet.data.remote.api.UserApi
import com.example.duriannet.data.remote.dtos.request.user.*
import com.example.duriannet.data.remote.dtos.response.NewUserDto
import com.example.duriannet.data.remote.dtos.response.UserDetailsDto
import com.example.duriannet.data.remote.dtos.response.UserProfileDto
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import javax.inject.Inject

class UserRepository @Inject constructor(private val userApi: UserApi) {
    suspend fun login(username: String, password: String): Result<NewUserDto> {
        return runCatching {
            val response = userApi.login(LoginRequestDto(username, password))
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Empty response from server")
            } else {
                // Extract the error message from the server response
                val errorMessage = response.errorBody()?.string() ?: "Login failed"
                throw Exception(errorMessage) // Pass the error message for the ViewModel to handle
            }
        }
    }

    suspend fun register(username: String, email: String, password: String) = runCatching {
        userApi.register(RegisterRequestDto(username, email, password))
    }

    suspend fun changePassword(username: String, currentPassword: String, newPassword: String): Result<Unit> {
        return runCatching {
            val response = userApi.changePassword(username, ChangePasswordRequestDto(currentPassword, newPassword))
            if (!response.isSuccessful) {
                // Attempt to parse the error response body
                val errorResponse = response.errorBody()?.string()
                val errorMessage = try {
                    // Assuming the error response is a list of objects
                    val errorList = Gson().fromJson(errorResponse, List::class.java)
                    (errorList[0] as? Map<*, *>)?.get("description")?.toString() ?: "Unknown error"
                } catch (e: Exception) {
                    "Failed to parse error: ${response.message()}"
                }
                throw Exception(errorMessage)
            }
        }
    }

    suspend fun getProfile(username: String): Result<UserProfileDto> {
        return runCatching {
            val response = userApi.getUserByUsername(username)
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Empty response from server")
            } else {
                throw Exception("Failed to fetch profile: ${response.message()}")
            }
        }
    }

    suspend fun updateProfile(username: String, request: UpdateUserProfileRequestDto) = runCatching {
        userApi.updateUser(username, request)
    }

    suspend fun forgotPassword(email: String) = runCatching {
        userApi.forgotPassword(ForgotPasswordRequestDto(email))
    }

    suspend fun resetPassword(newPassword: String, email: String) = runCatching {
        userApi.resetPassword(ResetPasswordRequestDto(email, newPassword))
    }

    suspend fun validateOTP(email: String, otp: String): Result<Unit> {
        return runCatching {
            val response = userApi.validateOTP(ValidateOTPRequestDto(email, otp))
            if (!response.isSuccessful) {
                // Extract error message from response
                val errorMessage = response.errorBody()?.string() ?: "Invalid OTP or OTP expired"
                throw Exception(errorMessage)
            }
        }
    }

    suspend fun deleteAccount(username: String): Result<Unit> {
        return runCatching {
            val response = userApi.deleteAccount(username)
            if (!response.isSuccessful) throw Exception("Failed to delete account: ${response.message()}")
        }
    }

    suspend fun logout(): Result<Unit> {
        return runCatching {
            val response = userApi.logout()
            if (!response.isSuccessful) throw Exception("Failed to log out: ${response.message()}")
        }
    }

    suspend fun updateProfileWithImage(
        username: String,
        fullName: String,
        email: String,
        phoneNumber: String,
        profilePicture: MultipartBody.Part
    ): Result<Unit> {
        return runCatching {
            val requestBodyMap = mapOf(
                "fullName" to fullName.toRequestBody("text/plain".toMediaTypeOrNull()),
                "email" to email.toRequestBody("text/plain".toMediaTypeOrNull()),
                "phoneNumber" to phoneNumber.toRequestBody("text/plain".toMediaTypeOrNull())
            )
            userApi.updateProfileWithImage(username, requestBodyMap, profilePicture)
        }
    }

    suspend fun getAllUsers(): List<UserDetailsDto>? {
        return try {
            val response = userApi.getAllUsers()
            if (response.isSuccessful) {
                response.body()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateUserWithoutImage(username: String, request: UpdateUserProfileRequestDto) = runCatching {
        userApi.updateUserWithoutImage(username, request)
    }

}

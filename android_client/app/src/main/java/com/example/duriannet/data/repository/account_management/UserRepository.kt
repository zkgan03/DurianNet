package com.example.duriannet.data.repository.account_management

import com.example.duriannet.data.remote.api.UserApi
import com.example.duriannet.data.remote.dtos.request.user.*
import com.example.duriannet.data.remote.dtos.response.NewUserDto
import com.example.duriannet.data.remote.dtos.response.UserProfileDto
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
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

    /*suspend fun changePassword(username: String, currentPassword: String, newPassword: String) =
        runCatching {
            userApi.changePassword(username, ChangePasswordRequestDto(currentPassword, newPassword))
        }*/

    /*suspend fun changePassword(username: String, currentPassword: String, newPassword: String): Result<Unit> {
        return runCatching {
            val response = userApi.changePassword(username, ChangePasswordRequestDto(currentPassword, newPassword))
            if (response.isSuccessful) {
                // Success
            } else {
                // Extract the error message from the server response
                val errorMessage = response.errorBody()?.string() ?: "Failed to change password"
                throw Exception(errorMessage)
            }
        }
    }*/

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



    /*suspend fun getProfile(username: String) = runCatching {
        userApi.getUserByUsername(username)
    }*/

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
            userApi.validateOTP(ValidateOTPRequestDto(email, otp))
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


}

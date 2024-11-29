package com.example.duriannet.data.repository.account_management

import com.example.duriannet.data.remote.api.UserApi
import com.example.duriannet.data.remote.dtos.request.user.*
import com.example.duriannet.data.remote.dtos.response.NewUserDto
import com.example.duriannet.data.remote.dtos.response.UserProfileDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class UserRepository @Inject constructor(private val userApi: UserApi) {
    /*suspend fun login(username: String, password: String) = runCatching {
        userApi.login(LoginRequestDto(username, password))
    }*/
    suspend fun login(username: String, password: String): Result<NewUserDto> {
        return runCatching {
            val response = userApi.login(LoginRequestDto(username, password))
            if (response.isSuccessful) {
                response.body() ?: throw Exception("Empty response from server")
            } else {
                throw Exception("Login failed: ${response.message()}")
            }
        }
    }

    suspend fun register(username: String, email: String, password: String) = runCatching {
        userApi.register(RegisterRequestDto(username, email, password))
    }

    suspend fun changePassword(username: String, currentPassword: String, newPassword: String) =
        runCatching {
            userApi.changePassword(username, ChangePasswordRequestDto(currentPassword, newPassword))
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

}

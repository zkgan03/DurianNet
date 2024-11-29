package com.example.duriannet.data.remote.api

import com.example.duriannet.data.remote.dtos.request.user.*
import com.example.duriannet.data.remote.dtos.response.NewUserDto
import com.example.duriannet.data.remote.dtos.response.UserProfileDto
import retrofit2.Response
import retrofit2.http.*

interface UserApi {
    @POST("appApi/user/appLogin")
    suspend fun login(@Body request: LoginRequestDto): Response<NewUserDto>

    @POST("appApi/user/appRegister")
    suspend fun register(@Body request: RegisterRequestDto): Response<Unit>

    @PUT("appApi/user/appChangePassword/{username}")
    suspend fun changePassword(
        @Path("username") username: String,
        @Body request: ChangePasswordRequestDto
    ): Response<Unit>

    @GET("appApi/user/appGetUserByUsername/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): Response<UserProfileDto>

    @PUT("appApi/user/appUpdateUserByUsername/{username}")
    suspend fun updateUser(
        @Path("username") username: String,
        @Body request: UpdateUserProfileRequestDto
    ): Response<Unit>

    @POST("appApi/user/appForgotPassword")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto): Response<Unit>

    @POST("appApi/user/appResetPassword")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): Response<Unit>
}

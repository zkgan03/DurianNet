package com.example.duriannet.data.remote.api

import com.example.duriannet.data.remote.dtos.request.user.*
import com.example.duriannet.data.remote.dtos.response.NewUserDto
import com.example.duriannet.data.remote.dtos.response.UserProfileDto
import okhttp3.MultipartBody
import okhttp3.RequestBody
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

    @Multipart
    @PUT("appApi/user/appUpdateUserByUsername/{username}")
    suspend fun updateProfileWithImage(
        @Path("username") username: String,
        @PartMap requestBodyMap: Map<String, @JvmSuppressWildcards RequestBody>,
        @Part profilePicture: MultipartBody.Part
    ): Response<Unit>



    @POST("appApi/user/appForgotPassword")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequestDto): Response<Unit>

    @POST("appApi/user/appResetPassword")
    suspend fun resetPassword(@Body request: ResetPasswordRequestDto): Response<Unit>

    @POST("appApi/user/validateOTP")
    suspend fun validateOTP(@Body request: ValidateOTPRequestDto): Response<Unit>

    @PUT("appApi/user/appDeleteAccount/{username}")
    suspend fun deleteAccount(@Path("username") username: String): Response<Unit>

    @POST("appApi/user/appLogout")
    suspend fun logout(): Response<Unit>


}
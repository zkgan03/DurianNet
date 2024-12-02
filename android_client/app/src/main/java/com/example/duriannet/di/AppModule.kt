package com.example.duriannet.di

import android.content.Context
import android.content.SharedPreferences
import com.example.duriannet.data.remote.api.CommentApi
import com.example.duriannet.data.remote.api.DurianApi
import com.example.duriannet.utils.Constant.SERVER_BASE_URL
import com.example.duriannet.data.remote.api.SellerApi
import com.example.duriannet.data.remote.api.UserApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton
import okhttp3.logging.HttpLoggingInterceptor


@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    // Provide Retrofit instance (shared for all APIs)
    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        val loggingInterceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY // Log request and response body
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()

        return Retrofit.Builder()
            .baseUrl(SERVER_BASE_URL) // Use the base URL from constants
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // Provide SellerApi instance
    @Provides
    @Singleton
    fun provideSellerApi(retrofit: Retrofit): SellerApi {

        return retrofit
            .newBuilder()
            .baseUrl(SERVER_BASE_URL + "api/Seller/")
            .build()
            .create(SellerApi::class.java)
    }

    // Provide CommentApi instance
    @Provides
    @Singleton
    fun provideCommentApi(retrofit: Retrofit): CommentApi {
        return retrofit
            .newBuilder()
            .baseUrl(SERVER_BASE_URL + "api/Comment/")
            .build()
            .create(CommentApi::class.java)
    }

    // Provide UserApi instance
    @Provides
    @Singleton
    fun provideUserApi(retrofit: Retrofit): UserApi {
        return retrofit.create(UserApi::class.java)
    }

    // Provide DurianApi instance
    @Provides
    @Singleton
    fun provideDurianApi(retrofit: Retrofit): DurianApi {
        return retrofit.create(DurianApi::class.java)
    }

    // Provide SharedPreferences instance
    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context
    ): SharedPreferences {
        return context.getSharedPreferences("DurianNetPrefs", Context.MODE_PRIVATE)
    }
}
/*@InstallIn(SingletonComponent::class)
@Module
object AppModule {

    @Provides
    @Singleton
    fun provideSellerApi(): SellerApi {
        return Retrofit.Builder()
            .baseUrl(SERVER_BASE_URL + "api/Seller/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(SellerApi::class.java)
    }

    @Provides
    @Singleton
    fun provideCommentApi(): CommentApi {
        return Retrofit.Builder()
            .baseUrl(SERVER_BASE_URL + "api/Comment/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(CommentApi::class.java)
    }

    //here start user and durian

*//*    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(SERVER_BASE_URL) // Base URL for your APIs
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }*//*

    @Provides
    @Singleton
    fun provideUserApi(): UserApi {
        return Retrofit.Builder()
            .baseUrl(SERVER_BASE_URL + "appApi/user/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(UserApi::class.java)
    }

    @Provides
    @Singleton
    fun provideDurianApi(): DurianApi {
        return Retrofit.Builder()
            .baseUrl(SERVER_BASE_URL + "appApi/durian/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(DurianApi::class.java)
    }

*//*    @Provides
    fun provideUserApi(retrofit: Retrofit): UserApi = retrofit.create(UserApi::class.java)*//*

    *//*@Provides
    fun provideDurianApi(retrofit: Retrofit): DurianApi = retrofit.create(DurianApi::class.java)*//*

    @Provides
    @Singleton
    fun provideSharedPreferences(
        @ApplicationContext context: Context // Use @ApplicationContext to specify application context
    ): SharedPreferences {
        return context.getSharedPreferences("DurianNetPrefs", Context.MODE_PRIVATE)
    }*/








    /*
    * @Binds annotation establishes the relationship between an interface and its implementation.
    * @Provides annotation is used to create and provide instances of dependencies,
    * @Provides can be used to provide instances of any type, including interfaces, classes, or other objects.
    * */


//    @Provides
//    @Singleton
//    fun provideSellerLocatorRepository(sellerLocatorApi: SellerLocatorApi): ISellerLocator {
//        return SellerLocatorRepository(sellerLocatorApi)
//    }
/*
}*/

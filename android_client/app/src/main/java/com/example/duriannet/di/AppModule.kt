package com.example.duriannet.di

import com.example.duriannet.data.remote.api.CommentApi
import com.example.duriannet.utils.Constant.SERVER_BASE_URL
import com.example.duriannet.data.remote.api.SellerApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
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
}
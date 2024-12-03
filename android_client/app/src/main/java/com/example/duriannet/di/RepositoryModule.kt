package com.example.duriannet.di

import com.example.duriannet.data.remote.api.DurianApi
import com.example.duriannet.data.remote.api.UserApi
import com.example.duriannet.data.repository.account_management.UserRepository
import com.example.duriannet.data.repository.comment.CommentRepository
import com.example.duriannet.data.repository.comment.ICommentRepository
import com.example.duriannet.data.repository.durian_dictionary.ChatbotRepository
import com.example.duriannet.data.repository.durian_dictionary.DurianRepository
import com.example.duriannet.data.repository.seller.ISellerRepository
import com.example.duriannet.data.repository.seller.SellerRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
abstract class RepositoryModule {

    /*
    * @Binds annotation establishes the relationship between an interface and its implementation.
    * @Provides annotation is used to create and provide instances of dependencies,
    * @Provides can be used to provide instances of any type, including interfaces, classes, or other objects.
    * */

    @Binds
    @Singleton
    abstract fun bindSellerRepository(
        impl: SellerRepository,
    ): ISellerRepository

    @Binds
    @Singleton
    abstract fun bindCommentRepository(
        impl: CommentRepository,
    ): ICommentRepository

    //user and durian

    companion object {
        @Provides
        fun provideUserRepository(userApi: UserApi): UserRepository = UserRepository(userApi)

        @Provides
        fun provideDurianRepository(durianApi: DurianApi): DurianRepository = DurianRepository(durianApi)
    }

}
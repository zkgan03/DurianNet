package com.example.duriannet.di

import com.example.duriannet.data.repository.seller_locator.ISellerLocator
import com.example.duriannet.data.repository.seller_locator.SellerLocatorRepository
import dagger.Binds
import dagger.Module
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
    abstract fun bindSellerLocatorRepository(
        impl: SellerLocatorRepository,
    ): ISellerLocator

}
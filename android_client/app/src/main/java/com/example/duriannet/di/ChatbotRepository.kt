package com.example.duriannet.di

import com.example.duriannet.data.remote.api.DurianApi
import com.example.duriannet.data.repository.durian_dictionary.ChatbotRepository
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@InstallIn(SingletonComponent::class)
@Module
object ChatbotRepositoryModule {

    @Provides
    @Singleton
    fun provideChatbotRepository(durianApi: DurianApi): ChatbotRepository {
        return ChatbotRepository(durianApi)
    }
}
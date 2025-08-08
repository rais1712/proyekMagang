// File: app/src/main/java/com/proyek/maganggsp/di/RepositoryModule.kt
package com.proyek.maganggsp.di

import com.proyek.maganggsp.data.repositoryImpl.AuthRepositoryImpl
import com.proyek.maganggsp.data.repositoryImpl.HistoryRepositoryImpl
import com.proyek.maganggsp.data.repositoryImpl.LoketRepositoryImpl
import com.proyek.maganggsp.domain.repository.AuthRepository
import com.proyek.maganggsp.domain.repository.HistoryRepository
import com.proyek.maganggsp.domain.repository.LoketRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLoketRepository(impl: LoketRepositoryImpl): LoketRepository

    @Binds
    @Singleton
    abstract fun bindHistoryRepository(impl: HistoryRepositoryImpl): HistoryRepository
}
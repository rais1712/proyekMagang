package com.proyek.maganggsp.di

import com.proyek.maganggsp.data.repository.AuthRepositoryImpl
import com.proyek.maganggsp.data.repository.LoketRepositoryImpl
import com.proyek.maganggsp.domain.repository.AuthRepository
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
    abstract fun bindAuthRepository(
        authRepositoryImpl: AuthRepositoryImpl
    ): AuthRepository

    @Binds
    @Singleton
    abstract fun bindLoketRepository(
        loketRepositoryImpl: LoketRepositoryImpl
    ): LoketRepository
}

// File: app/src/main/java/com/proyek/maganggsp/di/RepositoryModule.kt - UNIFIED DEPENDENCIES
package com.proyek.maganggsp.di

import com.proyek.maganggsp.data.repositoryImpl.AuthRepositoryImpl
import com.proyek.maganggsp.data.repositoryImpl.LoketRepositoryImpl
import com.proyek.maganggsp.data.repositoryImpl.ProfileRepositoryImpl
import com.proyek.maganggsp.domain.repository.AuthRepository
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.domain.repository.ProfileRepository
import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    /**
     * KEEP: AuthRepository untuk login functionality dengan AuthApi
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    /**
     * UNIFIED: LoketRepository menggunakan ProfileApi untuk real endpoints
     * Handles: Profile data, Transaction logs, Block/Unblock operations
     */
    @Binds
    @Singleton
    abstract fun bindLoketRepository(impl: LoketRepositoryImpl): LoketRepository

    /**
     * UNIFIED: ProfileRepository juga menggunakan ProfileApi
     * Backward compatibility untuk existing ProfileRepository usage
     */
    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    companion object {
        @Provides
        @Singleton
        fun provideExceptionMapper(): ExceptionMapper = ExceptionMapper()
    }
}
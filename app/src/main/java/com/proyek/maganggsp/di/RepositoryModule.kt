// ============================================================================
// UNIFIED REPOSITORY MODULE
// ============================================================================

// File: app/src/main/java/com/proyek/maganggsp/di/RepositoryModule.kt - COMPLETE REFACTOR
package com.proyek.maganggsp.di

import com.proyek.maganggsp.data.repositoryImpl.AuthRepositoryImpl
import com.proyek.maganggsp.data.repositoryImpl.UnifiedRepositoryImpl
import com.proyek.maganggsp.domain.repository.AuthRepository
import com.proyek.maganggsp.domain.repository.UnifiedRepository
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
     * KEEP: AuthRepository untuk login functionality
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    /**
     * NEW: UnifiedRepository replaces ProfileRepository, LoketRepository
     * Single source untuk semua API operations
     */
    @Binds
    @Singleton
    abstract fun bindUnifiedRepository(impl: UnifiedRepositoryImpl): UnifiedRepository

    companion object {
        @Provides
        @Singleton
        fun provideExceptionMapper(): ExceptionMapper = ExceptionMapper()
    }
}
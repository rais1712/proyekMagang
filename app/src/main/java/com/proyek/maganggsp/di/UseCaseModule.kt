// File: app/src/main/java/com/proyek/maganggsp/di/UseCaseModule.kt - NEW MODULAR
package com.proyek.maganggsp.di

import com.proyek.maganggsp.domain.usecase.auth.*
import com.proyek.maganggsp.domain.usecase.profile.*
import com.proyek.maganggsp.domain.usecase.transaction.*
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * MODULAR: Use case module untuk modular dependency injection
 * Replaces unified use case approach with separate modular use cases
 */
@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {

    /**
     * Note: Use cases are automatically provided by Hilt when they have @Inject constructor
     * This module exists for future custom provisions if needed
     *
     * Current modular use cases:
     *
     * AUTH USE CASES:
     * - LoginUseCase
     * - LogoutUseCase
     * - IsLoggedInUseCase
     * - GetAdminProfileUseCase
     *
     * PROFILE USE CASES:
     * - GetProfileUseCase
     * - SearchProfilesUseCase
     * - UpdateProfileUseCase
     * - GetRecentProfilesUseCase
     * - BlockUnblockUseCase
     *
     * TRANSACTION USE CASES:
     * - GetTransactionLogsUseCase
     */
}
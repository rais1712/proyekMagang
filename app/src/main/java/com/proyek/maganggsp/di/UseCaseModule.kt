// File: app/src/main/java/com/proyek/maganggsp/di/UseCaseModule.kt

package com.proyek.maganggsp.di

import com.proyek.maganggsp.domain.usecase.auth.*
// DISABLED: import com.proyek.maganggsp.domain.usecase.profile.*
// DISABLED: import com.proyek.maganggsp.domain.usecase.transaction.*
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * MINIMAL VERSION: Module for dependency injection
 * Use cases with @Inject constructor are automatically provided by Hilt
 *
 * Currently active:
 * - AUTH USE CASES: LoginUseCase, LogoutUseCase, IsLoggedInUseCase, GetAdminProfileUseCase
 *
 * Temporarily disabled:
 * - PROFILE USE CASES (waiting for ProfileRepositoryImpl fix)
 * - TRANSACTION USE CASES (waiting for TransactionRepositoryImpl fix)
 */
@Module
@InstallIn(SingletonComponent::class)
class UseCaseModule {
    // All use cases are provided automatically via @Inject constructors
    // This module is kept for documentation and future custom provisions
}

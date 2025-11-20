// File: app/src/main/java/com/proyek/maganggsp/di/RepositoryModule.kt

package com.proyek.maganggsp.di

import com.proyek.maganggsp.data.repositoryImpl.AuthRepositoryImpl
// DISABLED: import com.proyek.maganggsp.data.repositoryImpl.ProfileRepositoryImpl
// DISABLED: import com.proyek.maganggsp.data.repositoryImpl.TransactionRepositoryImpl
import com.proyek.maganggsp.domain.repository.AuthRepository
// DISABLED: import com.proyek.maganggsp.domain.repository.ProfileRepository
// DISABLED: import com.proyek.maganggsp.domain.repository.TransactionRepository
import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * MINIMAL VERSION: Only AuthRepository enabled for build success
 * ProfileRepository and TransactionRepository temporarily disabled
 *
 * TODO: Re-enable other repositories after fixing implementation files
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    /* TEMPORARILY DISABLED - Uncomment after fixing implementations

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository
    */

    companion object {
        @Provides
        @Singleton
        fun provideExceptionMapper(): ExceptionMapper = ExceptionMapper()
    }
}

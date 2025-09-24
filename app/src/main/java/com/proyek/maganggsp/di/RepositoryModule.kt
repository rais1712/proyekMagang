// File: app/src/main/java/com/proyek/maganggsp/di/RepositoryModule.kt - UPDATED FOR MODULAR
package com.proyek.maganggsp.di

import com.proyek.maganggsp.data.repositoryImpl.AuthRepositoryImpl
import com.proyek.maganggsp.data.repositoryImpl.ProfileRepositoryImpl
import com.proyek.maganggsp.data.repositoryImpl.TransactionRepositoryImpl
import com.proyek.maganggsp.domain.repository.AuthRepository
import com.proyek.maganggsp.domain.repository.ProfileRepository
import com.proyek.maganggsp.domain.repository.TransactionRepository
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
     * MODULAR: Separate repository bindings replacing unified approach
     */
    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    @Binds
    @Singleton
    abstract fun bindTransactionRepository(impl: TransactionRepositoryImpl): TransactionRepository

    companion object {
        @Provides
        @Singleton
        fun provideExceptionMapper(): ExceptionMapper = ExceptionMapper()
    }
}
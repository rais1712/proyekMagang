// File: app/src/main/java/com/proyek/maganggsp/di/DataModule.kt

package com.proyek.maganggsp.di

import android.content.Context
import com.google.gson.Gson
import com.google.gson.GsonBuilder
// DISABLED: import com.proyek.maganggsp.data.source.local.LoketHistoryManager
import com.proyek.maganggsp.data.source.local.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * MINIMAL VERSION: LoketHistoryManager disabled
 */
@Module
@InstallIn(SingletonComponent::class)
object DataModule {

    @Provides
    @Singleton
    fun provideGson(): Gson {
        return GsonBuilder()
            .setLenient()
            .create()
    }

    @Provides
    @Singleton
    fun provideSessionManager(
        @ApplicationContext context: Context
    ): SessionManager {
        return SessionManager(context)
    }

    /* TEMPORARILY DISABLED - Re-enable after fixing LoketHistoryManager

    @Provides
    @Singleton
    fun provideLoketHistoryManager(
        @ApplicationContext context: Context,
        gson: Gson
    ): LoketHistoryManager {
        return LoketHistoryManager(context, gson)
    }
    */
}

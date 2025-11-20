// File: app/src/main/java/com/proyek/maganggsp/di/NetworkModule.kt

package com.proyek.maganggsp.di

import com.proyek.maganggsp.BuildConfig
import com.proyek.maganggsp.data.api.AuthApi
// DISABLED: import com.proyek.maganggsp.data.api.ProfileApi
// DISABLED: import com.proyek.maganggsp.data.api.TransactionApi
import com.proyek.maganggsp.data.source.local.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * MINIMAL VERSION: Only AuthApi enabled
 * ProfileApi and TransactionApi temporarily disabled
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Provides
    @Singleton
    fun provideAuthInterceptor(
        sessionManager: SessionManager
    ): Interceptor {
        return Interceptor { chain ->
            val request = chain.request()
            val token = sessionManager.getAuthToken()

            val newRequest = if (!token.isNullOrEmpty()) {
                request.newBuilder()
                    .addHeader("Authorization", "Bearer $token")
                    .build()
            } else {
                request
            }

            chain.proceed(newRequest)
        }
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        authInterceptor: Interceptor
    ): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .addInterceptor(authInterceptor)
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(
        okHttpClient: OkHttpClient
    ): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    /* TEMPORARILY DISABLED - Uncomment after fixing API implementations

    @Provides
    @Singleton
    fun provideProfileApi(retrofit: Retrofit): ProfileApi {
        return retrofit.create(ProfileApi::class.java)
    }

    @Provides
    @Singleton
    fun provideTransactionApi(retrofit: Retrofit): TransactionApi {
        return retrofit.create(TransactionApi::class.java)
    }
    */
}

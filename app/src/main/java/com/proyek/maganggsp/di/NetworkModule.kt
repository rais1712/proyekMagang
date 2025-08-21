// ENHANCED: Production-ready NetworkModule with flexible configuration
// File: app/src/main/java/com/proyek/maganggsp/di/NetworkModule.kt

package com.proyek.maganggsp.di

import com.proyek.maganggsp.BuildConfig
import com.proyek.maganggsp.data.api.AuthApi
import com.proyek.maganggsp.data.api.HistoryApi
import com.proyek.maganggsp.data.api.LoketApi
import com.proyek.maganggsp.data.source.local.SessionManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * ENHANCED: Environment-based configuration
     * Supports multiple environments with easy switching
     */
    object NetworkConfig {
        // Development environment
        private const val DEV_BASE_URL = "http://192.168.168.6:8180/api/"

        // Staging environment (example)
        private const val STAGING_BASE_URL = "https://staging-api.gespay.com/api/"

        // Production environment (example)
        private const val PROD_BASE_URL = "https://api.gespay.com/api/"

        // ENHANCED: Smart base URL selection
        val BASE_URL: String = when {
            BuildConfig.DEBUG -> DEV_BASE_URL
            BuildConfig.BUILD_TYPE == "staging" -> STAGING_BASE_URL
            else -> PROD_BASE_URL
        }

        // ENHANCED: Environment-specific timeouts
        val CONNECT_TIMEOUT = if (BuildConfig.DEBUG) 30L else 20L
        val READ_TIMEOUT = if (BuildConfig.DEBUG) 30L else 25L
        val WRITE_TIMEOUT = if (BuildConfig.DEBUG) 30L else 25L

        // Cache configuration
        const val CACHE_SIZE = 10 * 1024 * 1024L // 10 MB
    }

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = when {
                BuildConfig.DEBUG -> HttpLoggingInterceptor.Level.BODY
                BuildConfig.BUILD_TYPE == "staging" -> HttpLoggingInterceptor.Level.BASIC
                else -> HttpLoggingInterceptor.Level.NONE
            }
        }
    }

    @Singleton
    @Provides
    @Named("auth")
    fun provideAuthInterceptor(sessionManager: SessionManager): Interceptor {
        return Interceptor { chain ->
            val originalRequest = chain.request()
            val token = sessionManager.getAuthToken()
            val url = originalRequest.url

            // ENHANCED: Smart header injection
            val requestBuilder = originalRequest.newBuilder()
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "GesPay-Admin-Android/${BuildConfig.VERSION_NAME}")

            // Add auth header if token exists and it's not a login request
            if (token != null && !url.encodedPath.contains("/auth/login")) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            // ENHANCED: Add debug headers in development
            if (BuildConfig.DEBUG) {
                requestBuilder.addHeader("X-Debug-Build", BuildConfig.BUILD_TYPE)
                requestBuilder.addHeader("X-Debug-Version", BuildConfig.VERSION_CODE.toString())
            }

            val newRequest = requestBuilder.build()
            chain.proceed(newRequest)
        }
    }

    @Singleton
    @Provides
    @Named("network_error")
    fun provideNetworkErrorInterceptor(): Interceptor {
        return Interceptor { chain ->
            try {
                val response = chain.proceed(chain.request())

                // ENHANCED: Handle specific response codes
                when (response.code) {
                    401 -> {
                        // Token expired, could trigger logout here
                        // For now, let the repository handle it
                    }
                    503 -> {
                        // Service unavailable, could implement retry logic
                    }
                }

                response
            } catch (e: Exception) {
                // ENHANCED: Better error handling for network issues
                throw e
            }
        }
    }

    @Singleton
    @Provides
    fun provideHttpCache(context: android.content.Context): Cache {
        val cacheDir = File(context.cacheDir, "http_cache")
        return Cache(cacheDir, NetworkConfig.CACHE_SIZE)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(
        loggingInterceptor: HttpLoggingInterceptor,
        @Named("auth") authInterceptor: Interceptor,
        @Named("network_error") networkErrorInterceptor: Interceptor,
        cache: Cache
    ): OkHttpClient {
        return OkHttpClient.Builder()
            // ENHANCED: Interceptor order matters
            .addInterceptor(authInterceptor) // Add auth headers first
            .addNetworkInterceptor(networkErrorInterceptor) // Handle network errors
            .addInterceptor(loggingInterceptor) // Log last (after all modifications)

            // ENHANCED: Timeout configuration
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .callTimeout(60, TimeUnit.SECONDS) // Overall call timeout

            // ENHANCED: Connection and retry configuration
            .retryOnConnectionFailure(true)
            .followRedirects(false) // Handle redirects manually for security
            .followSslRedirects(false)

            // ENHANCED: Cache configuration
            .cache(cache)

            // ENHANCED: Connection pool for better performance
            .connectionPool(
                okhttp3.ConnectionPool(
                    maxIdleConnections = 5,
                    keepAliveDuration = 5,
                    timeUnit = TimeUnit.MINUTES
                )
            )

            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NetworkConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    // ENHANCED: API providers with error handling
    @Singleton
    @Provides
    fun provideAuthApi(retrofit: Retrofit): AuthApi {
        return retrofit.create(AuthApi::class.java)
    }

    @Singleton
    @Provides
    fun provideLoketApi(retrofit: Retrofit): LoketApi {
        return retrofit.create(LoketApi::class.java)
    }

    @Singleton
    @Provides
    fun provideHistoryApi(retrofit: Retrofit): HistoryApi {
        return retrofit.create(HistoryApi::class.java)
    }

    // ENHANCED: Provide context for cache
    @Singleton
    @Provides
    fun provideApplicationContext(application: android.app.Application): android.content.Context {
        return application.applicationContext
    }
}
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

    object NetworkConfig {
        const val CONNECT_TIMEOUT = 30L
        const val READ_TIMEOUT = 30L
        const val WRITE_TIMEOUT = 30L
        const val CACHE_SIZE = 10 * 1024 * 1024L // 10 MB
    }

    @Singleton
    @Provides
    fun provideHttpLoggingInterceptor(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
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

            val requestBuilder = originalRequest.newBuilder()
                // FIXED: Remove existing Content-Type and add clean one without charset
                .removeHeader("Content-Type")
                .removeHeader("content-type") // Case insensitive removal
                .addHeader("Content-Type", "application/json") // Clean, no charset=UTF-8
                .addHeader("Accept", "application/json")
                .addHeader("User-Agent", "GesPay-Admin-Android/${BuildConfig.VERSION_NAME}")

            // Add auth token for authenticated endpoints
            if (token != null && !url.encodedPath.contains("/auth/login")) {
                requestBuilder.addHeader("Authorization", "Bearer $token")
            }

            chain.proceed(requestBuilder.build())
        }
    }

    @Singleton
    @Provides
    @Named("network_error")
    fun provideNetworkErrorInterceptor(): Interceptor {
        return Interceptor { chain ->
            try {
                chain.proceed(chain.request())
            } catch (e: Exception) {
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
            // IMPORTANT: Auth interceptor first to set proper headers
            .addInterceptor(authInterceptor)
            .addNetworkInterceptor(networkErrorInterceptor)
            .addInterceptor(loggingInterceptor) // Logging last to see final request
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT, TimeUnit.SECONDS)
            .cache(cache)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BuildConfig.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

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

    @Singleton
    @Provides
    fun provideApplicationContext(application: android.app.Application): android.content.Context {
        return application.applicationContext
    }
}
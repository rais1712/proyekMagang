package com.proyek.maganggsp.di

import com.proyek.maganggsp.data.api.AuthApi
import com.proyek.maganggsp.data.api.HistoryApi
import com.proyek.maganggsp.data.api.LoketApi
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    // Alamat dasar dari API lokal yang akan kita panggil
    private const val BASE_URL = "http://192.168.168.6:8180/"

    @Singleton
    @Provides
    fun provideOkHttpClient(): OkHttpClient {
        // Interceptor ini sangat berguna untuk melihat log request & response di Logcat
        // Membantu proses debugging saat ada masalah dengan API
        val loggingInterceptor = HttpLoggingInterceptor()
        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY)
        return OkHttpClient.Builder()
            .addInterceptor(loggingInterceptor)
            .build()
    }

    @Singleton
    @Provides
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Singleton
    @Provides
    fun provideAuthApi(retrofit: Retrofit): AuthApi = retrofit.create(AuthApi::class.java)

    @Singleton
    @Provides
    fun provideLoketApi(retrofit: Retrofit): LoketApi = retrofit.create(LoketApi::class.java)

    @Singleton
    @Provides
    fun provideHistoryApi(retrofit: Retrofit): HistoryApi = retrofit.create(HistoryApi::class.java)
}
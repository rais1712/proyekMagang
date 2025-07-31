package com.proyek.maganggsp.data.remote.api

import com.proyek.maganggsp.data.remote.dto.LoketDto
import retrofit2.Response
import retrofit2.http.GET

/**
 * Interface untuk endpoint-endpoint terkait History
 */
interface HistoryApi {
    @GET("history/recent")
    suspend fun getRecentHistory(): Response<List<LoketDto>>

    @GET("history/full")
    suspend fun getFullHistory(): Response<List<LoketDto>>
}

// File: app/src/main/java/com/proyek/maganggsp/data/api/HistoryApi.kt
package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.LoketDto
import retrofit2.http.GET

interface HistoryApi {
    /**
     * Mendapatkan daftar loket yang terakhir diakses oleh admin.
     */
    @GET("history/recent")
    suspend fun getRecentHistory(): List<LoketDto>

    /**
     * Mendapatkan semua riwayat pencarian loket oleh admin.
     */
    @GET("history/full")
    suspend fun getFullHistory(): List<LoketDto>
}
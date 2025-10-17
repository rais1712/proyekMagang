// File: app/src/main/java/com/proyek/maganggsp/data/api/TransactionApi.kt
package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.TransactionResponse
import retrofit2.Response
import retrofit2.http.*

/**
 * API interface untuk Transaction operations
 * Sesuai dengan HTTP requests yang diberikan
 */
interface TransactionApi {

    /**
     * Get transaction logs by PPID
     * Endpoint: GET /trx/ppid/{ppid}
     * Example: GET /trx/ppid/PIDLKTD0014
     */
    @GET("trx/ppid/{ppid}")
    suspend fun getTransactionLogs(
        @Path("ppid") ppid: String,
        @Header("Authorization") token: String,
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<List<TransactionResponse>>

    /**
     * Get transaction logs with date filter
     */
    @GET("trx/ppid/{ppid}")
    suspend fun getTransactionLogsWithFilter(
        @Path("ppid") ppid: String,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("limit") limit: Int? = null,
        @Header("Authorization") token: String,
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<List<TransactionResponse>>
}

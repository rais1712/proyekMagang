// File: app/src/main/java/com/proyek/maganggsp/data/api/TransactionApi.kt
package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.TransactionResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * MODULAR: TransactionApi interface
 * Based on actual HTTP request: GET /trx/ppid/{ppid}
 * Maps to TransactionLog domain model
 */
interface TransactionApi {

    /**
     * Get transaction logs for specific PPID
     * Endpoint: GET /trx/ppid/{ppid}
     * Example: /trx/ppid/PIDLKTD0014
     * Maps to: TransactionLog domain model
     */
    @GET("trx/ppid/{ppid}")
    suspend fun getTransactionLogs(@Path("ppid") ppid: String): List<TransactionResponse>
}


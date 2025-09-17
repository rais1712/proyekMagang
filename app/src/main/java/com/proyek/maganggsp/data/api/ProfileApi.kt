// File: app/src/main/java/com/proyek/maganggsp/data/api/ProfileApi.kt
package com.proyek.maganggsp.data.api

import retrofit2.Response
import retrofit2.http.*

/**
 * 🎯 PHASE 1: NEW PROFILE API INTERFACE
 * Based on actual API endpoints from HTTP requests provided
 */
interface ProfileApi {

    /**
     * Get profile information
     * Endpoint: GET /profiles/ppid/{ppid}
     * Example: /profiles/ppid/PIDLKTD0025 or /profiles/ppid/0000001
     */
    @GET("profiles/ppid/{ppid}")
    suspend fun getProfile(@Path("ppid") ppid: String): ProfileResponse

    /**
     * Get transaction logs for specific PPID
     * Endpoint: GET /trx/ppid/{ppid}
     * Example: /trx/ppid/PIDLKTD0014
     */
    @GET("trx/ppid/{ppid}")
    suspend fun getTransactions(@Path("ppid") ppid: String): List<TransactionResponse>

    /**
     * Update profile (for block/unblock operations)
     * Endpoint: PUT /profiles/ppid/{ppid}
     * Body: {"mpPpid": "PIDLKTD0025blok"} for block
     *       {"mpPpid": "PIDLKTD0025"} for unblock
     */
    @PUT("profiles/ppid/{ppid}")
    suspend fun updateProfile(
        @Path("ppid") ppid: String,
        @Body request: UpdateProfileRequest
    ): Response<Unit>
}
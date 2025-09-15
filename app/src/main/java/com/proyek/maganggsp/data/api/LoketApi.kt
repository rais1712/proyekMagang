// File: app/src/main/java/com/proyek/maganggsp/data/api/LoketApi.kt - ENHANCED
package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.LoketProfileResponse
import com.proyek.maganggsp.data.dto.LoketResponse
import com.proyek.maganggsp.data.dto.TransactionResponse
import com.proyek.maganggsp.data.dto.UpdateLoketProfileRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * ENHANCED: Loket API interface based on available endpoints
 * Focuses on profile management with existing backend endpoints
 */
interface LoketApi {
    /**
     * Get loket profile with comprehensive data
     * Maps to existing: GET /profiles/ppid/{ppid}
     */
    @GET("profiles/ppid/{ppid}")
    suspend fun getLoketProfile(@Path("ppid") ppid: String): LoketProfileResponse

    /**
     * Get transaction logs for specific loket
     * Maps to existing: GET /trx/ppid/{ppid}
     */
    @GET("trx/ppid/{ppid}")
    suspend fun getLoketTransactions(@Path("ppid") ppid: String): List<TransactionResponse>

    /**
     * Update loket profile information
     * Maps to existing: PUT /profiles/ppid/{ppid}
     */
    @PUT("profiles/ppid/{ppid}")
    suspend fun updateLoketProfile(
        @Path("ppid") ppid: String,
        @Body request: UpdateLoketProfileRequest
    ): Response<Unit>

    /**
     * Search for loket by phone number
     * Maps to existing: GET /search/loket
     */
    @GET("search/loket")
    suspend fun searchLoket(@Query("phone") phoneNumber: String): List<LoketResponse>

    /**
     * Block a loket
     * Maps to existing: PUT /loket/{ppid}/block
     */
    @PUT("loket/{ppid}/block")
    suspend fun blockLoket(@Path("ppid") ppid: String): Response<Unit>

    /**
     * Unblock a loket
     * Maps to existing: PUT /loket/{ppid}/unblock
     */
    @PUT("loket/{ppid}/unblock")
    suspend fun unblockLoket(@Path("ppid") ppid: String): Response<Unit>

    /**
     * FUTURE: Batch get multiple loket profiles
     * Can be implemented when backend supports it
     */
    @GET("profiles/batch")
    suspend fun getMultipleLoketProfiles(
        @Query("ppids") ppids: String // Comma-separated PPID list
    ): List<LoketProfileResponse>
}
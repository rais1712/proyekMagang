// File: app/src/main/java/com/proyek/maganggsp/data/api/ProfileApi.kt
package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.ProfileResponse
import com.proyek.maganggsp.data.dto.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * MODULAR: ProfileApi interface
 * Based on actual HTTP request: GET /profiles/ppid/{ppid}
 * Maps to Receipt domain model for profile display
 */
interface ProfileApi {

    /**
     * Get profile information for profile card display
     * Endpoint: GET /profiles/ppid/{ppid}
     * Example: /profiles/ppid/PIDLKTD0025 or /profiles/ppid/0000001
     * Maps to: Receipt domain model
     */
    @GET("profiles/ppid/{ppid}")
    suspend fun getProfile(@Path("ppid") ppid: String): ProfileResponse

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
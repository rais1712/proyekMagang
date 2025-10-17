// File: app/src/main/java/com/proyek/maganggsp/data/api/ProfileApi.kt
package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.ProfileResponse
import com.proyek.maganggsp.data.dto.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * API interface untuk Profile operations
 * Sesuai dengan HTTP requests yang diberikan
 */
interface ProfileApi {

    /**
     * Get profile by PPID
     * Endpoint: GET /profiles/ppid/{ppid}
     * Example: GET /profiles/ppid/PIDLKTD0025
     */
    @GET("profiles/ppid/{ppid}")
    suspend fun getProfile(
        @Path("ppid") ppid: String,
        @Header("Authorization") token: String,
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<ProfileResponse>

    /**
     * Update/Block profile by PPID
     * Endpoint: PUT /profiles/ppid/{ppid}
     * Example: PUT /profiles/ppid/PIDLKTD0025
     * Body: {"mpPpid": "PIDLKTD0025blok"} untuk block
     */
    @PUT("profiles/ppid/{ppid}")
    suspend fun updateProfile(
        @Path("ppid") ppid: String,
        @Body request: UpdateProfileRequest,
        @Header("Authorization") token: String,
        @Header("Content-Type") contentType: String = "application/json"
    ): Response<Unit>

    /**
     * Block loket - helper method
     */
    suspend fun blockLoket(ppid: String, token: String): Response<Unit> {
        return updateProfile(
            ppid = ppid,
            request = UpdateProfileRequest(mpPpid = "${ppid}blok"),
            token = token
        )
    }

    /**
     * Unblock loket - helper method
     */
    suspend fun unblockLoket(ppid: String, token: String): Response<Unit> {
        return updateProfile(
            ppid = ppid,
            request = UpdateProfileRequest(mpPpid = ppid), // tanpa "blok"
            token = token
        )
    }
}

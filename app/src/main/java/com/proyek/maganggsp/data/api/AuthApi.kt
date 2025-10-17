// File: app/src/main/java/com/proyek/maganggsp/data/api/AuthApi.kt

package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.LoginRequest
import com.proyek.maganggsp.data.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * API interface untuk Authentication operations
 * ✅ Already modular - no changes needed
 */
interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}

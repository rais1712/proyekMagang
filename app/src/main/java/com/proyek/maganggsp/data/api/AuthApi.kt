// File: app/src/main/java/com/proyek/maganggsp/data/api/AuthApi.kt
package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.LoginRequest
import com.proyek.maganggsp.data.dto.LoginResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    /**
     * FIXED: Updated endpoint path sesuai dengan informasi yang diberikan
     * Path: /api/auth/login (sudah include /api di BASE_URL NetworkModule)
     * Method: POST
     * Headers: Content-Type: application/json (handled by interceptor)
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>
}
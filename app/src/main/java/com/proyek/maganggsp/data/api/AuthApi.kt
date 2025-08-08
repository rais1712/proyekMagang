// File: app/src/main/java/com/proyek/maganggsp/data/api/AuthApi.kt
package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.LoginRequest
import com.proyek.maganggsp.data.dto.LoginResponse
import retrofit2.http.Body
import retrofit2.http.POST

interface AuthApi {
    /**
     * Mengirim kredensial login untuk mendapatkan token otentikasi.
     */
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
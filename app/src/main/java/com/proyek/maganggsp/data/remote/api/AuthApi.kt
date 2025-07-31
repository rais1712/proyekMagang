package com.proyek.maganggsp.data.remote.api

import com.proyek.maganggsp.data.remote.dto.LoginResponseDto
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

/**
 * Interface untuk autentikasi API
 */
interface AuthApi {
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequest
    ): Response<LoginResponseDto>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>
}

data class LoginRequest(
    val email: String,
    val password: String
)

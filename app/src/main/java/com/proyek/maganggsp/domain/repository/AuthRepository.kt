package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.util.NetworkResult

/**
 * Interface repository untuk autentikasi
 */
interface AuthRepository {
    suspend fun login(email: String, password: String): NetworkResult<Admin>
    suspend fun logout(): NetworkResult<Unit>
    suspend fun getStoredToken(): String?
    suspend fun clearStoredToken()
}

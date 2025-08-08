package com.proyek.maganggsp.data.repositoryImpl

import com.proyek.maganggsp.data.api.AuthApi
import com.proyek.maganggsp.data.dto.LoginRequest
import com.proyek.maganggsp.data.dto.toDomain
import com.proyek.maganggsp.data.source.local.SessionManager
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.repository.AuthRepository
import javax.inject.Inject

// ... (import)
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    override suspend fun login(email: String, password: String): Admin {
        return try {
            val request = LoginRequest(email, password)
            val response = api.login(request)
            val admin = response.adminData.toDomain()

            // Simpan token DAN profil admin
            sessionManager.saveAuthToken(response.token)
            sessionManager.saveAdminProfile(admin) // <<< TAMBAHKAN BARIS INI

            admin
        } catch (e: Exception) {
            throw e
        }
    }
}
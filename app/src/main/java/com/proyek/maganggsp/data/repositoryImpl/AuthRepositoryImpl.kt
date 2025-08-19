package com.proyek.maganggsp.data.repositoryImpl

import android.util.Log
import com.proyek.maganggsp.data.api.AuthApi
import com.proyek.maganggsp.data.dto.LoginRequest
import com.proyek.maganggsp.data.dto.toDomain
import com.proyek.maganggsp.data.source.local.SessionManager
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.repository.AuthRepository
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionManager: SessionManager
) : AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    override suspend fun login(email: String, password: String): Admin {
        return try {
            Log.d(TAG, "Starting login process for email: $email")

            val request = LoginRequest(email, password)
            Log.d(TAG, "Sending login request to API")

            val response = api.login(request)
            Log.d(TAG, "Login API response received - AdminName: ${response.adminName}, AdminEmail: ${response.adminEmail}, Token exists: ${response.token != null}")

            // FIXED: Direct conversion without .adminData
            val admin = response.toDomain()
            Log.d(TAG, "Response mapped to domain - Admin: ${admin.name}, Email: ${admin.email}")

            // FIXED: Save token from mapped admin object
            sessionManager.saveAuthToken(admin.token)
            Log.d(TAG, "Token saved to session")

            // Save admin profile
            sessionManager.saveAdminProfile(admin)
            Log.d(TAG, "Admin profile saved to session")

            // Debug session state
            Log.d(TAG, sessionManager.debugSessionState())

            Log.d(TAG, "Login process completed successfully")
            admin
        } catch (e: Exception) {
            Log.e(TAG, "Login failed with exception: ${e.message}", e)
            throw e
        }
    }
}
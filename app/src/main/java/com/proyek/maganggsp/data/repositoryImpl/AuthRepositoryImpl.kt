// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/AuthRepositoryImpl.kt
package com.proyek.maganggsp.data.repositoryImpl

import android.util.Log
import com.proyek.maganggsp.data.api.AuthApi
import com.proyek.maganggsp.data.dto.LoginRequest
import com.proyek.maganggsp.data.dto.toDomain
import com.proyek.maganggsp.data.source.local.SessionManager
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.repository.AuthRepository
import com.proyek.maganggsp.util.exceptions.AppException
import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionManager: SessionManager,
    private val exceptionMapper: ExceptionMapper
) : BaseRepository(exceptionMapper), AuthRepository {

    companion object {
        private const val TAG = "AuthRepository"
    }

    override suspend fun login(email: String, password: String): Admin {
        return try {
            Log.d(TAG, "Starting login process for email: $email")

            // Validate input parameters
            if (email.isBlank()) {
                throw AppException.ValidationException("Email tidak boleh kosong")
            }
            if (password.isBlank()) {
                throw AppException.ValidationException("Password tidak boleh kosong")
            }

            val request = LoginRequest(email, password)
            Log.d(TAG, "Sending login request to API")

            // FIXED: Handle Response<LoginResponse> properly
            val response = api.login(request)

            if (!response.isSuccessful) {
                Log.e(TAG, "Login failed with HTTP ${response.code()}: ${response.message()}")
                throw retrofit2.HttpException(response)
            }

            val loginResponse = response.body()
                ?: throw AppException.ParseException("Response body kosong dari server")

            Log.d(TAG, "Login API response received - AdminName: ${loginResponse.adminName}, AdminEmail: ${loginResponse.adminEmail}, Token exists: ${loginResponse.token != null}")

            // Validate response data
            if (loginResponse.token.isNullOrBlank()) {
                throw AppException.AuthenticationException("Token tidak diterima dari server")
            }

            // Convert to domain model
            val admin = loginResponse.toDomain()
            Log.d(TAG, "Response mapped to domain - Admin: ${admin.name}, Email: ${admin.email}")

            // Save session data
            sessionManager.saveAuthToken(admin.token)
            sessionManager.saveAdminProfile(admin)
            Log.d(TAG, "Login session saved successfully")

            // Debug session state
            Log.d(TAG, sessionManager.debugSessionState())

            Log.d(TAG, "Login process completed successfully")
            admin

        } catch (e: AppException) {
            Log.e(TAG, "Login failed with AppException: ${e.message}", e)
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Login failed with unexpected exception: ${e.message}", e)
            // Let the ExceptionMapper handle this
            throw exceptionMapper.mapToAppException(e)
        }
    }

    /**
     * Additional method untuk logout (clear session)
     */
    override suspend fun logout() {
        try {
            Log.d(TAG, "Logging out user")
            sessionManager.clearSession()
            Log.d(TAG, "Session cleared successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout: ${e.message}", e)
            throw AppException.UnknownException("Gagal melakukan logout", e)
        }
    }

    /**
     * Method untuk check apakah user sudah login
     */
    override fun isLoggedIn(): Boolean {
        return sessionManager.isSessionValid()
    }
}
// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/AuthRepositoryImpl.kt - FIXED FOR MODULAR
package com.proyek.maganggsp.data.repositoryImpl

import android.util.Log
import com.proyek.maganggsp.BuildConfig
import com.proyek.maganggsp.data.api.AuthApi
import com.proyek.maganggsp.data.api.LoginRequest
import com.proyek.maganggsp.data.api.toAdmin
import com.proyek.maganggsp.data.source.local.SessionManager
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.repository.AuthRepository
import com.proyek.maganggsp.util.exceptions.AppException
import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import retrofit2.HttpException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject

/**
 * FIXED: AuthRepositoryImpl using modular AuthApi interface
 * Maintains existing functionality with modular API structure
 */
class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionManager: SessionManager,
    private val exceptionMapper: ExceptionMapper
) : BaseRepository(exceptionMapper), AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    override suspend fun login(email: String, password: String): Admin {
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "Login via modular AuthApi")
            Log.d(TAG, "Email: $email")
            Log.d(TAG, "Target URL: ${BuildConfig.BASE_URL}auth/login")
        }

        return try {
            // Input validation
            validateLoginInputs(email, password)

            // Create request
            val request = LoginRequest(email, password)
            Log.d(TAG, "Sending modular API request...")

            // Make API call via modular interface
            val response = api.login(request)
            Log.d(TAG, "Modular API response received - HTTP ${response.code()}")

            // Handle response
            if (!response.isSuccessful) {
                throw HttpException(response)
            }

            val loginResponse = response.body()
                ?: throw AppException.ParseException("Empty response from server")

            Log.d(TAG, "Login response via modular API:")
            Log.d(TAG, "  Token received: ${loginResponse.token != null}")
            Log.d(TAG, "  Email: ${loginResponse.email}")

            // Validate response data
            validateLoginResponse(loginResponse)

            // Convert to domain model using modular mapping
            val admin = loginResponse.toAdmin()

            // Save session
            val sessionSaved = saveUserSession(admin)
            if (!sessionSaved) {
                throw AppException.UnknownException("Failed to save login session")
            }

            Log.d(TAG, "Login completed via modular API")
            admin

        } catch (e: AppException) {
            Log.e(TAG, "Login failed with AppException: ${e.message}")
            throw e
        } catch (e: HttpException) {
            val mappedException = mapHttpException(e)
            Log.e(TAG, "HTTP error: ${e.code()} -> ${mappedException.message}")
            throw mappedException
        } catch (e: UnknownHostException) {
            val exception = AppException.NetworkException(
                "Server unreachable. Ensure development server is running at 192.168.168.6:8180"
            )
            Log.e(TAG, "UnknownHostException: ${exception.message}")
            throw exception
        } catch (e: ConnectException) {
            val exception = AppException.NetworkException(
                "Cannot connect to server. Ensure server is running and network is active."
            )
            Log.e(TAG, "ConnectException: ${exception.message}")
            throw exception
        } catch (e: SocketTimeoutException) {
            val exception = AppException.NetworkException(
                "Connection timeout. Server may be slow."
            )
            Log.e(TAG, "SocketTimeoutException: ${exception.message}")
            throw exception
        } catch (e: Exception) {
            Log.e(TAG, "Unexpected error during login", e)
            throw AppException.UnknownException("An unexpected error occurred: ${e.message}")
        }
    }

    override suspend fun logout() {
        try {
            Log.d(TAG, "Starting logout process")
            val sessionCleared = sessionManager.clearSession()

            if (sessionCleared) {
                Log.d(TAG, "Session cleared successfully")
            } else {
                Log.w(TAG, "Session clear returned false, but continuing")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error during logout", e)
            sessionManager.clearSession()
            throw AppException.UnknownException("Logout failed: ${e.message}")
        }
    }

    override fun isLoggedIn(): Boolean {
        return sessionManager.isSessionValid()
    }

    // Helper methods
    private fun validateLoginInputs(email: String, password: String) {
        if (email.isBlank()) {
            throw AppException.ValidationException("Email cannot be empty")
        }
        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            throw AppException.ValidationException("Invalid email format")
        }
        if (password.isBlank()) {
            throw AppException.ValidationException("Password cannot be empty")
        }
        if (password.length < 6) {
            throw AppException.ValidationException("Password must be at least 6 characters")
        }
    }

    private fun validateLoginResponse(response: com.proyek.maganggsp.data.api.LoginResponse) {
        if (response.token.isNullOrBlank()) {
            throw AppException.ParseException("Invalid token received from server")
        }
        if (response.email.isNullOrBlank()) {
            throw AppException.ParseException("Invalid email received from server")
        }
    }

    private fun saveUserSession(admin: Admin): Boolean {
        return try {
            val tokenSaved = sessionManager.saveAuthToken(admin.token)
            val profileSaved = sessionManager.saveAdminProfile(admin)
            tokenSaved && profileSaved
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save session", e)
            false
        }
    }

    private fun mapHttpException(e: HttpException): AppException {
        return when (e.code()) {
            401 -> AppException.AuthenticationException("Email atau password salah")
            403 -> AppException.AuthenticationException("Akses ditolak")
            else -> AppException.ServerException(e.code(), "Terjadi kesalahan server")
        }
    }
}
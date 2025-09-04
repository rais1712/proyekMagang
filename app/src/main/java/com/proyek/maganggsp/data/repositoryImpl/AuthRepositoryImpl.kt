// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/AuthRepositoryImpl.kt - BUG FIX
package com.proyek.maganggsp.data.repositoryImpl

import android.util.Log
import com.proyek.maganggsp.BuildConfig
import com.proyek.maganggsp.data.api.AuthApi
import com.proyek.maganggsp.data.dto.LoginRequest
import com.proyek.maganggsp.data.dto.toDomain
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

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi,
    private val sessionManager: SessionManager,
    private val exceptionMapper: ExceptionMapper
) : BaseRepository(exceptionMapper), AuthRepository {

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }

    override suspend fun login(email: String, password: String): Admin {
        // BUG FIX: Debug logging only in debug builds (no FeatureFlags)
        if (BuildConfig.DEBUG) {
            Log.d(TAG, "🚀 Starting API login process")
            Log.d(TAG, "📧 Email: $email")
            Log.d(TAG, "🔐 Password length: ${password.length}")
            Log.d(TAG, "🌐 Target URL: ${BuildConfig.BASE_URL}auth/login")
        }

        return try {
            // Input validation
            validateLoginInputs(email, password)

            // Create request
            val request = LoginRequest(email, password)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "📤 Sending API request...")
            }

            // Make API call
            val response = api.login(request)
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "📨 API response received")
                Log.d(TAG, "✅ HTTP Status: ${response.code()}")
                Log.d(TAG, "📄 Response successful: ${response.isSuccessful}")
            }

            // Handle response
            if (!response.isSuccessful) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "❌ API call failed - HTTP ${response.code()}")
                }
                throw HttpException(response)
            }

            val loginResponse = response.body()
            if (loginResponse == null) {
                if (BuildConfig.DEBUG) {
                    Log.e(TAG, "❌ Response body is null")
                }
                throw AppException.ParseException("Empty response from server")
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "📋 Response data:")
                Log.d(TAG, "  🔑 Token received: ${loginResponse.token != null}")
                Log.d(TAG, "  📧 Email: ${loginResponse.email}")
                Log.d(TAG, "  👤 Role: ${loginResponse.role}")
            }

            // Validate response data
            validateLoginResponse(loginResponse)

            // Convert to domain model
            val admin = loginResponse.toDomain()
            if (BuildConfig.DEBUG) {
                Log.d(TAG, "🔄 Converted to domain model:")
                Log.d(TAG, "  👤 Name: ${admin.name}")
                Log.d(TAG, "  📧 Email: ${admin.email}")
            }

            // Save session
            val sessionSaved = saveUserSession(admin)
            if (!sessionSaved) {
                throw AppException.UnknownException("Failed to save login session")
            }

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "✅ Login process completed successfully")
            }

            admin

        } catch (e: AppException) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "❌ Login failed with AppException: ${e.message}", e)
            }
            throw e
        } catch (e: HttpException) {
            val mappedException = mapHttpException(e)
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "❌ HTTP error: ${e.code()} -> ${mappedException.message}")
            }
            throw mappedException
        } catch (e: UnknownHostException) {
            val exception = AppException.NetworkException(
                "Server unreachable. Ensure development server is running at 192.168.168.6:8180"
            )
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "❌ UnknownHostException: ${exception.message}")
            }
            throw exception
        } catch (e: ConnectException) {
            val exception = AppException.NetworkException(
                "Cannot connect to server. Ensure server is running and network is active."
            )
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "❌ ConnectException: ${exception.message}")
            }
            throw exception
        } catch (e: SocketTimeoutException) {
            val exception = AppException.NetworkException(
                "Connection timeout. Server may be slow."
            )
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "❌ SocketTimeoutException: ${exception.message}")
            }
            throw exception
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "❌ Unexpected error during login", e)
            }
            throw AppException.UnknownException("An unexpected error occurred: ${e.message}")
        }
    }

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

    private fun validateLoginResponse(response: com.proyek.maganggsp.data.dto.LoginResponse) {
        if (response.token.isNullOrBlank()) {
            throw AppException.ParseException("Invalid token received from server")
        }
        if (response.email.isNullOrBlank()) {
            throw AppException.ParseException("Invalid email received from server")
        }
        if (response.role.isNullOrBlank()) {
            throw AppException.ParseException("Invalid role received from server")
        }
    }

    private fun saveUserSession(admin: Admin): Boolean {
        return try {
            sessionManager.saveAuthToken(admin.token)
            sessionManager.saveUserEmail(admin.email)
            sessionManager.saveUserRole(admin.role)
            true
        } catch (e: Exception) {
            if (BuildConfig.DEBUG) {
                Log.e(TAG, "Failed to save session", e)
            }
            false
        }
    }

    private fun mapHttpException(e: HttpException): AppException {
        return when (e.code()) {
            401 -> AppException.AuthenticationException("Invalid email or password")
            403 -> AppException.AuthenticationException("Access denied")
            404 -> AppException.ParseException("Login endpoint not found")
            500 -> AppException.ServerException("Internal server error")
            else -> AppException.UnknownException("Unknown error occurred: HTTP ${e.code()}")
        }
    }
}

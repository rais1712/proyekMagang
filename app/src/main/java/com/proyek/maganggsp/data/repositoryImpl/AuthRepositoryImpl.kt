// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/AuthRepositoryImpl.kt
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
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
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
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "📤 Sending API request...")
            }

            // Make API call
            val response = api.login(request)
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "📨 API response received")
                Log.d(TAG, "✅ HTTP Status: ${response.code()}")
                Log.d(TAG, "📄 Response successful: ${response.isSuccessful}")
                Log.d(TAG, "📋 Response headers: ${response.headers()}")
                if (!response.isSuccessful) {
                    Log.e(TAG, "❌ Error response body: ${response.errorBody()?.string()}")
                }
            }

            // Handle response
            if (!response.isSuccessful) {
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.e(TAG, "❌ API call failed - HTTP ${response.code()}")
                    Log.e(TAG, "📄 Error body: ${response.errorBody()?.string()}")
                }
                throw HttpException(response)
            }

            val loginResponse = response.body()
            if (loginResponse == null) {
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.e(TAG, "❌ Response body is null")
                }
                throw AppException.ParseException("Response kosong dari server")
            }

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "📋 Response data:")
                Log.d(TAG, "  🔑 Token received: ${loginResponse.token != null}")
                Log.d(TAG, "  🔑 Token length: ${loginResponse.token?.length ?: 0}")
                Log.d(TAG, "  📧 Email: ${loginResponse.email}")
                Log.d(TAG, "  👤 Role: ${loginResponse.role}")
            }

            // Validate response data
            validateLoginResponse(loginResponse)

            // Convert to domain model
            val admin = loginResponse.toDomain()
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🔄 Converted to domain model:")
                Log.d(TAG, "  👤 Name: ${admin.name}")
                Log.d(TAG, "  📧 Email: ${admin.email}")
                Log.d(TAG, "  🔑 Token: ${admin.token.take(10)}...")
            }

            // Save session
            val sessionSaved = saveUserSession(admin)
            if (!sessionSaved) {
                throw AppException.UnknownException("Gagal menyimpan sesi login")
            }

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "✅ Login process completed successfully")
                Log.d(TAG, "📊 Session state: ${sessionManager.debugSessionState()}")
            }

            admin

        } catch (e: AppException) {
            // App exceptions are already user-friendly
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.e(TAG, "❌ Login failed with AppException: ${e.message}", e)
            }
            throw e
        } catch (e: HttpException) {
            // HTTP exceptions need mapping
            val mappedException = mapHttpException(e)
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.e(TAG, "❌ HTTP error: ${e.code()} -> ${mappedException.message}")
            }
            throw mappedException
        } catch (e: UnknownHostException) {
            val exception = AppException.NetworkException(
                "Server tidak dapat dijangkau. Pastikan server development berjalan di 192.168.168.6:8180"
            )
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.e(TAG, "❌ UnknownHostException: ${exception.message}")
            }
            throw exception
        } catch (e: ConnectException) {
            val exception = AppException.NetworkException(
                "Tidak dapat terhubung ke server. Pastikan server berjalan dan jaringan aktif."
            )
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.e(TAG, "❌ ConnectException: ${exception.message}")
            }
            throw exception
        } catch (e: SocketTimeoutException) {
            val exception = AppException.NetworkException(
                "Koneksi timeout. Server mungkin sedang lambat."
            )
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.e(TAG, "❌ SocketTimeoutException: ${exception.message}")
            }
            throw exception
        } catch (e: Exception) {
            // Unexpected exceptions
            val mappedException = exceptionMapper.mapToAppException(e)
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.e(TAG, "❌ Unexpected exception: ${e.message}", e)
                Log.e(TAG, "🔄 Mapped to: ${mappedException.message}")
            }
            throw mappedException
        }
    }

    private fun validateLoginInputs(email: String, password: String) {
        when {
            email.isBlank() -> throw AppException.ValidationException("Email tidak boleh kosong")
            password.isBlank() -> throw AppException.ValidationException("Password tidak boleh kosong")
            !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() ->
                throw AppException.ValidationException("Format email tidak valid")
            password.length < 6 ->
                throw AppException.ValidationException("Password minimal 6 karakter")
        }
    }

    private fun validateLoginResponse(loginResponse: com.proyek.maganggsp.data.dto.LoginResponse) {
        when {
            loginResponse.token.isNullOrBlank() ->
                throw AppException.AuthenticationException("Token tidak diterima dari server")
            loginResponse.email.isNullOrBlank() ->
                throw AppException.ParseException("Email tidak diterima dari server")
            loginResponse.role.isNullOrBlank() ->
                throw AppException.ParseException("Role tidak diterima dari server")
        }
    }

    private fun saveUserSession(admin: Admin): Boolean {
        return try {
            val tokenSaved = sessionManager.saveAuthToken(admin.token)
            val profileSaved = sessionManager.saveAdminProfile(admin)

            val success = tokenSaved && profileSaved
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "💾 Session save result - Token: $tokenSaved, Profile: $profileSaved")
            }
            success
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.e(TAG, "❌ Failed to save session", e)
            }
            false
        }
    }

    private fun mapHttpException(httpException: HttpException): AppException {
        val code = httpException.code()
        return when (code) {
            401 -> AppException.AuthenticationException("Email atau password salah")
            404 -> AppException.NetworkException("Server tidak ditemukan (404)")
            422 -> AppException.ValidationException("Data login tidak sesuai format")
            500 -> AppException.ServerException(code, "Server mengalami masalah internal")
            502, 503 -> AppException.NetworkException("Server sedang maintenance")
            else -> AppException.ServerException(code, "Kesalahan server HTTP $code")
        }
    }

    override suspend fun logout() {
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚪 Starting logout process")
        }

        try {
            sessionManager.clearSession()
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "✅ Logout completed - session cleared")
            }
        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.e(TAG, "❌ Logout error: ${e.message}", e)
            }
            throw AppException.UnknownException("Gagal melakukan logout", e)
        }
    }

    override fun isLoggedIn(): Boolean {
        val isValid = sessionManager.isSessionValid()
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🔍 Session check: $isValid")
        }
        return isValid
    }
}
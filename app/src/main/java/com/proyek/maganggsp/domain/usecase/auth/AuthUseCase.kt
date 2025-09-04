// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/auth/AuthUseCases.kt - COMPLETE
package com.proyek.maganggsp.domain.usecase.auth

import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.repository.AuthRepository
import com.proyek.maganggsp.data.source.local.SessionManager
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import android.util.Log

/**
 * ✅ PHASE 1 COMPLETE: All Auth UseCases with enhanced error handling
 */

// =====================================================================
// LOGIN USE CASE - Enhanced with better validation and error mapping
// =====================================================================
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    companion object {
        private const val TAG = "LoginUseCase"
    }

    operator fun invoke(email: String, password: String): Flow<Resource<Admin>> = flow {
        try {
            emit(Resource.Loading())
            Log.d(TAG, "🚀 Starting login process for email: $email")

            // Enhanced input validation
            when {
                email.isBlank() -> {
                    emit(Resource.Error(AppException.ValidationException("Email tidak boleh kosong")))
                    return@flow
                }
                password.isBlank() -> {
                    emit(Resource.Error(AppException.ValidationException("Password tidak boleh kosong")))
                    return@flow
                }
                !isValidEmail(email) -> {
                    emit(Resource.Error(AppException.ValidationException("Format email tidak valid")))
                    return@flow
                }
                password.length < 6 -> {
                    emit(Resource.Error(AppException.ValidationException("Password minimal 6 karakter")))
                    return@flow
                }
            }

            // Perform login through repository
            val admin = repository.login(email.trim(), password)
            emit(Resource.Success(admin))
            Log.d(TAG, "✅ Login successful for: ${admin.email}")

        } catch (e: AppException) {
            Log.e(TAG, "❌ AppException during login: ${e.message}")
            emit(Resource.Error(e))
        } catch (e: HttpException) {
            val message = when (e.code()) {
                401 -> "Email atau password salah"
                404 -> "Server tidak ditemukan"
                500 -> "Terjadi kesalahan pada server"
                else -> "Terjadi kesalahan: ${e.message()}"
            }
            Log.e(TAG, "❌ HTTP error during login: ${e.code()} - $message")
            emit(Resource.Error(AppException.ServerException(e.code(), message)))
        } catch (e: IOException) {
            Log.e(TAG, "❌ Network error during login: ${e.message}")
            emit(Resource.Error(AppException.NetworkException("Periksa koneksi internet Anda")))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error during login", e)
            emit(Resource.Error(AppException.UnknownException("Terjadi kesalahan yang tidak terduga")))
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

// =====================================================================
// LOGOUT USE CASE - Complete implementation
// =====================================================================
class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    companion object {
        private const val TAG = "LogoutUseCase"
    }

    operator fun invoke(): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            Log.d(TAG, "🚪 Starting logout process")

            repository.logout()
            emit(Resource.Success(Unit))
            Log.d(TAG, "✅ Logout successful")

        } catch (e: AppException) {
            Log.e(TAG, "❌ AppException during logout: ${e.message}")
            emit(Resource.Error(e))
        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error during logout", e)
            emit(Resource.Error(AppException.UnknownException("Gagal melakukan logout: ${e.message}")))
        }
    }
}

// =====================================================================
// LOGIN STATUS USE CASE - Complete implementation
// =====================================================================
class IsLoggedInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    companion object {
        private const val TAG = "IsLoggedInUseCase"
    }

    operator fun invoke(): Boolean {
        return try {
            val isLoggedIn = repository.isLoggedIn()
            Log.d(TAG, "🔍 Login status check: $isLoggedIn")
            isLoggedIn
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error checking login status", e)
            false
        }
    }
}

// =====================================================================
// GET ADMIN PROFILE USE CASE - Enhanced with error handling
// =====================================================================
class GetAdminProfileUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "GetAdminProfileUseCase"
    }

    operator fun invoke(): Admin? {
        return try {
            val admin = sessionManager.getAdminProfile()
            if (admin != null) {
                Log.d(TAG, "👤 Admin profile retrieved: ${admin.name}")
            } else {
                Log.w(TAG, "⚠️ No admin profile found in session")
            }
            admin
        } catch (e: Exception) {
            Log.e(TAG, "❌ Error retrieving admin profile", e)
            null
        }
    }
}

// =====================================================================
// VALIDATE SESSION USE CASE - New addition
// =====================================================================
class ValidateSessionUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "ValidateSessionUseCase"
    }

    operator fun invoke(): Flow<Resource<Admin>> = flow {
        try {
            emit(Resource.Loading())
            Log.d(TAG, "🔍 Validating user session")

            if (!repository.isLoggedIn()) {
                Log.w(TAG, "❌ Session not valid - user not logged in")
                emit(Resource.Error(AppException.AuthenticationException("Session expired")))
                return@flow
            }

            val admin = sessionManager.getAdminProfile()
            if (admin == null) {
                Log.w(TAG, "❌ Session not valid - no admin profile")
                emit(Resource.Error(AppException.AuthenticationException("Profile not found")))
                return@flow
            }

            Log.d(TAG, "✅ Session valid for: ${admin.name}")
            emit(Resource.Success(admin))

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error validating session", e)
            emit(Resource.Error(AppException.UnknownException("Session validation failed")))
        }
    }
}

// =====================================================================
// REFRESH SESSION USE CASE - New addition for token refresh scenarios
// =====================================================================
class RefreshSessionUseCase @Inject constructor(
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "RefreshSessionUseCase"
    }

    operator fun invoke(): Flow<Resource<Admin>> = flow {
        try {
            emit(Resource.Loading())
            Log.d(TAG, "🔄 Refreshing session data")

            val admin = sessionManager.getAdminProfile()
            val hasValidSession = sessionManager.isSessionValid()

            if (admin != null && hasValidSession) {
                Log.d(TAG, "✅ Session refreshed successfully")
                emit(Resource.Success(admin))
            } else {
                Log.w(TAG, "❌ Session refresh failed - invalid or expired")
                emit(Resource.Error(AppException.AuthenticationException("Session expired")))
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error refreshing session", e)
            emit(Resource.Error(AppException.UnknownException("Failed to refresh session")))
        }
    }
}

// =====================================================================
// SESSION DEBUG USE CASE - For troubleshooting
// =====================================================================
class GetSessionDebugInfoUseCase @Inject constructor(
    private val sessionManager: SessionManager,
    private val repository: AuthRepository
) {
    companion object {
        private const val TAG = "GetSessionDebugInfoUseCase"
    }

    operator fun invoke(): String {
        return try {
            val admin = sessionManager.getAdminProfile()
            val isLoggedIn = repository.isLoggedIn()
            val sessionDebug = sessionManager.debugSessionState()
            val remainingTime = sessionManager.getRemainingSessionTimeMinutes()

            """
            📊 AUTH DEBUG INFORMATION:
            
            🔐 Authentication Status:
            - Is Logged In: $isLoggedIn
            - Session Valid: ${sessionManager.isSessionValid()}
            - Remaining Time: ${remainingTime} minutes
            
            👤 User Profile:
            - Name: ${admin?.name ?: "NOT_FOUND"}
            - Email: ${admin?.email ?: "NOT_FOUND"}
            - Token Length: ${admin?.token?.length ?: 0}
            
            💾 Session Details:
            $sessionDebug
            
            🏗️ Build Info:
            - Build Type: ${com.proyek.maganggsp.BuildConfig.BUILD_TYPE}
            - Base URL: ${com.proyek.maganggsp.BuildConfig.BASE_URL}
            - Version: ${com.proyek.maganggsp.BuildConfig.VERSION_NAME}
            """.trimIndent()

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error generating debug info", e)
            "Session Debug Error: ${e.message}"
        }
    }
}
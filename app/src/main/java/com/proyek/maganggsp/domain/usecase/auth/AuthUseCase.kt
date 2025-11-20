// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/auth/AuthUseCase.kt

package com.proyek.maganggsp.domain.usecase.auth

import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.repository.AuthRepository
import com.proyek.maganggsp.data.source.local.SessionManager
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.collect
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject
import android.util.Log

/**
 * ✅ FIXED: All Auth UseCases with String-based error handling
 * ❌ REMOVED: All AppException usage
 * ✅ FIXED: Flow type consistency
 */

// =====================================================================
// LOGIN USE CASE - Fixed AppException to String conversion
// =====================================================================
class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    companion object {
        private const val TAG = "LoginUseCase"
    }

    suspend operator fun invoke(email: String, password: String): Flow<Resource<Admin>> = flow {
        try {
            emit(Resource.Loading())
            Log.d(TAG, "🚀 Starting login process for email: $email")

            // Enhanced input validation with String messages
            when {
                email.isBlank() -> {
                    emit(Resource.Error("Email tidak boleh kosong"))
                    return@flow
                }

                password.isBlank() -> {
                    emit(Resource.Error("Password tidak boleh kosong"))
                    return@flow
                }

                !isValidEmail(email) -> {
                    emit(Resource.Error("Format email tidak valid"))
                    return@flow
                }

                password.length < 6 -> {
                    emit(Resource.Error("Password minimal 6 karakter"))
                    return@flow
                }
            }

            // Call repository and collect Flow result
            repository.login(email.trim(), password).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Login successful for: ${result.data?.email}")
                        emit(result)
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Login failed: ${result.message}")
                        emit(result)
                    }
                    is Resource.Loading -> {
                        emit(result)
                    }
                    else -> {
                        emit(Resource.Error("Unknown response type"))
                    }
                }
            }

        } catch (e: HttpException) {
            val message = when (e.code()) {
                401 -> "Email atau password salah"
                404 -> "Server tidak ditemukan"
                500 -> "Terjadi kesalahan pada server"
                else -> "Terjadi kesalahan: ${e.message()}"
            }
            Log.e(TAG, "❌ HTTP error during login: ${e.code()} - $message")
            emit(Resource.Error(message))

        } catch (e: IOException) {
            Log.e(TAG, "❌ Network error during login: ${e.message}")
            emit(Resource.Error("Periksa koneksi internet Anda"))

        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error during login", e)
            emit(Resource.Error("Terjadi kesalahan yang tidak terduga: ${e.message}"))
        }
    }

    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

// =====================================================================
// LOGOUT USE CASE - Fixed to String errors
// =====================================================================
class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    companion object {
        private const val TAG = "LogoutUseCase"
    }

    suspend operator fun invoke(): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            Log.d(TAG, "🚪 Starting logout process")

            repository.logout()
            emit(Resource.Success(Unit))
            Log.d(TAG, "✅ Logout successful")

        } catch (e: Exception) {
            Log.e(TAG, "❌ Unexpected error during logout", e)
            emit(Resource.Error("Gagal melakukan logout: ${e.message}"))
        }
    }
}

// =====================================================================
// LOGIN STATUS USE CASE - Simple boolean check
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
// VALIDATE SESSION USE CASE - Fixed String errors
// =====================================================================
class ValidateSessionUseCase @Inject constructor(
    private val repository: AuthRepository,
    private val sessionManager: SessionManager
) {
    companion object {
        private const val TAG = "ValidateSessionUseCase"
    }

    suspend operator fun invoke(): Flow<Resource<Admin>> = flow {
        try {
            emit(Resource.Loading())
            Log.d(TAG, "🔍 Validating user session")

            if (!repository.isLoggedIn()) {
                Log.w(TAG, "❌ Session not valid - user not logged in")
                emit(Resource.Error("Session expired"))
                return@flow
            }

            val admin = sessionManager.getAdminProfile()
            if (admin == null) {
                Log.w(TAG, "❌ Session not valid - no admin profile")
                emit(Resource.Error("Profile not found"))
                return@flow
            }

            Log.d(TAG, "✅ Session valid for: ${admin.name}")
            emit(Resource.Success(admin))

        } catch (e: Exception) {
            Log.e(TAG, "❌ Error validating session", e)
            emit(Resource.Error("Session validation failed: ${e.message}"))
        }
    }
}


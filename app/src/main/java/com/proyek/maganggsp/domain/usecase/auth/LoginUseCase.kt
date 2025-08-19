// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/auth/LoginUseCase.kt
package com.proyek.maganggsp.domain.usecase.auth

import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.repository.AuthRepository
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<Resource<Admin>> = flow {
        try {
            emit(Resource.Loading())

            // Input validation
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

            // Perform login
            val admin = repository.login(email.trim(), password)
            emit(Resource.Success(admin))

        } catch (e: AppException) {
            emit(Resource.Error(e))
        } catch (e: HttpException) {
            val message = when (e.code()) {
                401 -> "Email atau password salah"
                404 -> "Server tidak ditemukan"
                500 -> "Terjadi kesalahan pada server"
                else -> "Terjadi kesalahan: ${e.message()}"
            }
            emit(Resource.Error(AppException.ServerException(e.code(), message)))
        } catch (e: IOException) {
            emit(Resource.Error(AppException.NetworkException("Periksa koneksi internet Anda")))
        } catch (e: Exception) {
            emit(Resource.Error(AppException.UnknownException("Terjadi kesalahan yang tidak terduga")))
        }
    }

    /**
     * Simple email validation
     */
    private fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }
}

/**
 * Logout Use Case
 */
class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            repository.logout()
            emit(Resource.Success(Unit))
        } catch (e: AppException) {
            emit(Resource.Error(e))
        } catch (e: Exception) {
            emit(Resource.Error(AppException.UnknownException("Gagal melakukan logout")))
        }
    }
}

/**
 * Check Login Status Use Case
 */
class IsLoggedInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return repository.isLoggedIn()
    }
}
// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/ValidatePpidUseCase.kt
package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for PPID validation with Indonesian-specific rules
 */
class ValidatePpidUseCase @Inject constructor() {

    operator fun invoke(ppid: String): Flow<Resource<String>> = flow {
        emit(Resource.Loading())

        try {
            val cleanedPpid = ppid.trim()

            when {
                cleanedPpid.isBlank() -> {
                    emit(Resource.Error(AppException.ValidationException("PPID tidak boleh kosong")))
                }
                cleanedPpid.length < 5 -> {
                    emit(Resource.Error(AppException.ValidationException("PPID harus minimal 5 karakter")))
                }
                cleanedPpid.length > 50 -> {
                    emit(Resource.Error(AppException.ValidationException("PPID terlalu panjang (maksimal 50 karakter)")))
                }
                !cleanedPpid.matches(Regex("^[A-Za-z0-9]+$")) -> {
                    emit(Resource.Error(AppException.ValidationException("PPID hanya boleh mengandung huruf dan angka")))
                }
                else -> {
                    emit(Resource.Success(cleanedPpid))
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(AppException.UnknownException("Validasi PPID gagal: ${e.message}")))
        }
    }

    /**
     * Quick validation for immediate feedback
     */
    fun validateQuick(ppid: String): ValidationResult {
        val cleaned = ppid.trim()

        return when {
            cleaned.isBlank() -> ValidationResult.Error("PPID tidak boleh kosong")
            cleaned.length < 5 -> ValidationResult.Error("PPID minimal 5 karakter")
            cleaned.length > 50 -> ValidationResult.Error("PPID maksimal 50 karakter")
            !cleaned.matches(Regex("^[A-Za-z0-9]+$")) -> ValidationResult.Error("Hanya huruf dan angka")
            else -> ValidationResult.Success(cleaned)
        }
    }

    sealed class ValidationResult {
        data class Success(val ppid: String) : ValidationResult()
        data class Error(val message: String) : ValidationResult()
    }
}
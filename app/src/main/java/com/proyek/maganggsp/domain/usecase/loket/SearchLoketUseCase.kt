// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/SearchLoketUseCase.kt - MVP CORE
package com.proyek.maganggsp.domain.usecase.loket

import android.util.Log
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * MVP CORE: Search Loket by Phone Number with Indonesian phone format support
 */
class SearchLoketUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    companion object {
        private const val TAG = "SearchLoketUseCase"
    }

    operator fun invoke(phoneNumber: String): Flow<Resource<List<Loket>>> = flow {
        try {
            emit(Resource.Loading())
            Log.d(TAG, "🔍 Starting phone search for: $phoneNumber")

            // Validate and format phone number
            val validationResult = validatePhoneNumber(phoneNumber)
            if (validationResult.isError) {
                emit(Resource.Error(AppException.ValidationException(validationResult.message)))
                return@flow
            }

            val formattedPhone = formatPhoneNumber(phoneNumber)
            Log.d(TAG, "📱 Formatted phone: $formattedPhone")

            // Perform search via repository
            repository.searchLoket(formattedPhone).collect { resource ->
                when (resource) {
                    is Resource.Loading -> emit(Resource.Loading())
                    is Resource.Success -> {
                        val results = resource.data
                        Log.d(TAG, "✅ Search results: ${results.size} lokets found")

                        if (results.isEmpty()) {
                            emit(Resource.Empty)
                        } else {
                            // Save successful search to history
                            results.forEach { loket ->
                                try {
                                    repository.saveToHistory(loket)
                                } catch (e: Exception) {
                                    Log.w(TAG, "Failed to save search history", e)
                                }
                            }
                            emit(Resource.Success(results))
                        }
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Search error: ${resource.exception.message}")
                        emit(resource)
                    }
                    is Resource.Empty -> {
                        Log.d(TAG, "🔍 No lokets found for phone: $formattedPhone")
                        emit(Resource.Empty)
                    }
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Search use case error", e)
            emit(Resource.Error(AppException.UnknownException("Pencarian gagal: ${e.message}")))
        }
    }

    /**
     * Validate Indonesian phone number formats
     */
    private fun validatePhoneNumber(phoneNumber: String): ValidationResult {
        val cleaned = phoneNumber.replace(Regex("[^0-9+]"), "")

        return when {
            cleaned.isBlank() -> ValidationResult.Error("Nomor telepon tidak boleh kosong")
            cleaned.length < 10 -> ValidationResult.Error("Nomor telepon terlalu pendek")
            cleaned.length > 15 -> ValidationResult.Error("Nomor telepon terlalu panjang")
            !isValidIndonesianFormat(cleaned) -> ValidationResult.Error("Format nomor tidak valid. Gunakan format 08xxx atau +62xxx")
            else -> ValidationResult.Success(cleaned)
        }
    }

    /**
     * Check if phone number follows Indonesian format
     */
    private fun isValidIndonesianFormat(phoneNumber: String): Boolean {
        return phoneNumber.startsWith("08") ||
                phoneNumber.startsWith("+62") ||
                phoneNumber.startsWith("628")
    }

    /**
     * Format phone number to consistent format for API
     */
    private fun formatPhoneNumber(phoneNumber: String): String {
        val cleaned = phoneNumber.replace(Regex("[^0-9+]"), "")

        return when {
            cleaned.startsWith("+62") -> cleaned
            cleaned.startsWith("62") -> "+$cleaned"
            cleaned.startsWith("08") -> "+62${cleaned.substring(1)}"
            else -> cleaned
        }.also {
            Log.d(TAG, "📱 Phone format: '$phoneNumber' -> '$it'")
        }
    }

    /**
     * Validation result sealed class
     */
    sealed class ValidationResult(val message: String) {
        data class Success(val phone: String) : ValidationResult("Valid")
        data class Error(val errorMessage: String) : ValidationResult(errorMessage)

        val isError: Boolean get() = this is Error
        val isSuccess: Boolean get() = this is Success
    }

    /**
     * Quick validation for UI feedback
     */
    fun validateQuick(phoneNumber: String): ValidationResult {
        return validatePhoneNumber(phoneNumber)
    }

    /**
     * Get example formats for UI hints
     */
    fun getPhoneFormatExamples(): List<String> {
        return listOf(
            "+628123456789",
            "08123456789",
            "628123456789"
        )
    }
}
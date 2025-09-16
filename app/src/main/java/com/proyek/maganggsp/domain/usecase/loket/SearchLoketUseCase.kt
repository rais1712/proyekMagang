package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchLoketUseCase @Inject constructor(
    private val loketRepository: LoketRepository
) {

    /**
     * Search loket by phone number (LOCAL CACHE ONLY - NO API)
     */
    operator fun invoke(phoneNumber: String): Flow<Resource<List<Loket>>> {
        return loketRepository.searchLoket(phoneNumber)
    }

    /**
     * Quick validation untuk phone number format
     */
    data class ValidationResult(
        val isValid: Boolean,
        val isError: Boolean,
        val message: String
    )

    fun validateQuick(phoneNumber: String): ValidationResult {
        return when {
            phoneNumber.isBlank() -> ValidationResult(false, true, "Nomor telepon tidak boleh kosong")
            phoneNumber.length < 3 -> ValidationResult(false, false, "Ketik minimal 3 angka")
            phoneNumber.length < 8 -> ValidationResult(false, true, "Nomor telepon terlalu pendek")
            !phoneNumber.matches(Regex("^(\\+62|62|08)[0-9]+$")) -> {
                ValidationResult(false, true, "Format nomor tidak valid. Gunakan 08xxx atau +62xxx")
            }
            else -> ValidationResult(true, false, "Valid")
        }
    }

    /**
     * Get example formats untuk UI hints
     */
    fun getPhoneFormatExamples(): List<String> {
        return listOf(
            "08123456789",
            "+628123456789",
            "628123456789"
        )
    }
}
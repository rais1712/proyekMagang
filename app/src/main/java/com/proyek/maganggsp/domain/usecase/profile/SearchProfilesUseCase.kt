// =================================================================
// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/profile/SearchProfilesUseCase.kt
package com.proyek.maganggsp.domain.usecase.profile

import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.repository.ProfileRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * MODULAR: Search profiles use case for PPID search
 */
class SearchProfilesUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(ppidQuery: String): Flow<Resource<List<Receipt>>> {
        return profileRepository.searchProfiles(ppidQuery)
    }

    /**
     * PPID validation helpers
     */
    data class ValidationResult(
        val isValid: Boolean,
        val isError: Boolean,
        val message: String
    )

    fun validatePpid(ppid: String): ValidationResult {
        return when {
            ppid.isBlank() -> ValidationResult(false, true, "PPID tidak boleh kosong")
            ppid.length < 5 -> ValidationResult(false, false, "Ketik minimal 5 karakter PPID")
            ppid.length < 8 -> ValidationResult(false, true, "PPID terlalu pendek")
            !isValidPpidFormat(ppid) -> {
                ValidationResult(false, true, "Format PPID tidak valid. Gunakan format PIDLKTD0025")
            }
            else -> ValidationResult(true, false, "Valid")
        }
    }

    private fun isValidPpidFormat(ppid: String): Boolean {
        val patterns = listOf(
            "^PIDLKTD\\d+.*$".toRegex(),
            "^[A-Z]{3,}[0-9]+.*$".toRegex()
        )
        return patterns.any { it.matches(ppid) }
    }

    fun getPpidFormatExamples(): List<String> {
        return listOf(
            "PIDLKTD0025",
            "PIDLKTD0025blok",
            "PIDLKTD0030"
        )
    }
}






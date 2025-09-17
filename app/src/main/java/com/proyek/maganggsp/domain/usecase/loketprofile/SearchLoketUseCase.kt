// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/SearchLoketUseCase.kt - UPDATED PPID SEARCH
package com.proyek.maganggsp.domain.usecase.loketprofile

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class SearchLoketUseCase @Inject constructor(
    private val loketRepository: LoketRepository
) {

    /**
     * UPDATED: Search loket by PPID (LOCAL CACHE ONLY - NO API)
     * Searches through local history and tries direct API access if exact PPID match
     */
    operator fun invoke(ppid: String): Flow<Resource<List<Loket>>> {
        return if (isValidPpidFormat(ppid)) {
            // If valid PPID format, try direct access + search history
            loketRepository.searchLoket(ppid)
        } else {
            // Invalid format, search in local cache only
            loketRepository.searchLoket(ppid)
        }
    }

    /**
     * UPDATED: Quick validation untuk PPID format
     */
    data class ValidationResult(
        val isValid: Boolean,
        val isError: Boolean,
        val message: String
    )

    fun validateQuick(ppid: String): ValidationResult {
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

    /**
     * PPID format validation
     */
    private fun isValidPpidFormat(ppid: String): Boolean {
        // Check if PPID matches common patterns
        val patterns = listOf(
            "^PIDLKTD\\d+.*$".toRegex(), // PIDLKTD0025, PIDLKTD0025blok
            "^[A-Z]{3,}[0-9]+.*$".toRegex() // Generic pattern
        )

        return patterns.any { it.matches(ppid) }
    }

    /**
     * Get example formats untuk UI hints
     */
    fun getPpidFormatExamples(): List<String> {
        return listOf(
            "PIDLKTD0025",
            "PIDLKTD0025blok",
            "PIDLKTD0030"
        )
    }

    /**
     * Extract clean PPID (remove blok suffix for search)
     */
    fun extractCleanPpid(ppid: String): String {
        return ppid.removeSuffix("blok")
    }

    /**
     * Check if PPID is blocked version
     */
    fun isBlockedPpid(ppid: String): Boolean {
        return ppid.endsWith("blok")
    }
}
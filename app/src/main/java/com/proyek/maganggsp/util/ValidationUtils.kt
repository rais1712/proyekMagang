// File: app/src/main/java/com/proyek/maganggsp/util/ValidationUtils.kt
package com.proyek.maganggsp.util

import android.util.Patterns

/**
 * MODULAR: Validation utilities
 * Extracted from unified AppUtils.kt untuk better modularity
 */
object ValidationUtils {

    /**
     * Validate PPID format
     */
    fun isValidPpid(ppid: String): Boolean {
        return ppid.isNotBlank() && ppid.length >= 5
    }

    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        return Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Enhanced PPID validation with format checking
     * Based on PPID patterns: PIDLKTD0025, PIDLKTD0025blok
     */
    fun validatePpidFormat(ppid: String): ValidationResult {
        return when {
            ppid.isBlank() -> ValidationResult(false, "PPID tidak boleh kosong")
            ppid.length < 5 -> ValidationResult(false, "PPID minimal 5 karakter")
            ppid.length < 8 -> ValidationResult(false, "PPID terlalu pendek")
            !ppid.matches("^[A-Z]{3,}[0-9]+.*$".toRegex()) -> {
                ValidationResult(false, "Format PPID tidak valid. Gunakan format PIDLKTD0025")
            }
            else -> ValidationResult(true, "Valid")
        }
    }

    /**
     * Validate phone number format
     */
    fun isValidPhoneNumber(phone: String): Boolean {
        val cleanPhone = phone.replace(Regex("[^0-9+]"), "")
        return when {
            cleanPhone.startsWith("+62") && cleanPhone.length >= 12 -> true
            cleanPhone.startsWith("08") && cleanPhone.length >= 10 -> true
            cleanPhone.startsWith("62") && cleanPhone.length >= 11 -> true
            else -> false
        }
    }

    /**
     * Check if PPID is blocked version
     */
    fun isBlockedPpid(ppid: String): Boolean {
        return ppid.endsWith("blok", ignoreCase = true)
    }

    /**
     * Get original PPID (remove blok suffix)
     */
    fun getOriginalPpid(ppid: String): String {
        return ppid.removeSuffix("blok").removeSuffix("BLOK")
    }

    /**
     * Get blocked PPID (add blok suffix)
     */
    fun getBlockedPpid(ppid: String): String {
        return if (isBlockedPpid(ppid)) ppid else "${ppid}blok"
    }

    /**
     * Validate password strength
     */
    fun isValidPassword(password: String): ValidationResult {
        return when {
            password.isBlank() -> ValidationResult(false, "Password tidak boleh kosong")
            password.length < 6 -> ValidationResult(false, "Password minimal 6 karakter")
            password.length > 128 -> ValidationResult(false, "Password terlalu panjang")
            else -> ValidationResult(true, "Valid")
        }
    }

    /**
     * Validate transaction amount
     */
    fun isValidAmount(amount: Long): Boolean {
        return amount != 0L && amount >= -999999999L && amount <= 999999999L
    }

    /**
     * Check if text contains only allowed characters for search
     */
    fun isValidSearchQuery(query: String): Boolean {
        return query.matches("^[A-Za-z0-9\\s]+$".toRegex())
    }

    /**
     * Validation result data class
     */
    data class ValidationResult(
        val isValid: Boolean,
        val message: String
    )

    /**
     * Get PPID format examples for UI hints
     */
    fun getPpidFormatExamples(): List<String> {
        return listOf(
            "PIDLKTD0025",
            "PIDLKTD0025blok",
            "PIDLKTD0030"
        )
    }

    /**
     * Validate required fields for profile
     */
    fun validateProfileFields(
        ppid: String,
        namaLoket: String,
        nomorHP: String
    ): ValidationResult {
        return when {
            ppid.isBlank() -> ValidationResult(false, "PPID wajib diisi")
            namaLoket.isBlank() -> ValidationResult(false, "Nama loket wajib diisi")
            nomorHP.isBlank() -> ValidationResult(false, "Nomor HP wajib diisi")
            !isValidPpid(ppid) -> ValidationResult(false, "Format PPID tidak valid")
            !isValidPhoneNumber(nomorHP) -> ValidationResult(false, "Format nomor HP tidak valid")
            else -> ValidationResult(true, "Valid")
        }
    }
}
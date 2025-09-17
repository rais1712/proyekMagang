// File: app/src/main/java/com/proyek/maganggsp/domain/model/UnifiedDomainModels.kt
package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 🎯 PHASE 1: UNIFIED DOMAIN MODELS
 * Primary models for new API structure: Receipt + TransactionLog focused
 * Based on API endpoints: /profiles/ppid/{ppid} and /trx/ppid/{ppid}
 */

// ============================================================================
// PRIMARY MODELS: Receipt & TransactionLog
// ============================================================================

/**
 * Receipt: Core data from /profiles/ppid/{ppid}
 * Represents profile info that will be displayed as receipt-like entries
 */
data class Receipt(
    val refNumber: String,           // Reference/ID for this profile access
    val idPelanggan: String,         // Customer ID (mapped from ppid)
    val amount: Long = 0L,           // Balance or transaction amount
    val logged: String,              // Access timestamp
    val ppid: String,                // Primary identifier for navigation

    // Extended profile information
    val namaLoket: String = "",      // Loket name for display
    val nomorHP: String = "",        // Phone number
    val email: String = "",          // Email address
    val alamat: String = "",         // Address
    val status: LoketStatus = LoketStatus.NORMAL
) {

    /**
     * Format amount as Indonesian Rupiah
     */
    fun getFormattedAmount(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(amount)
    }

    /**
     * Format logged timestamp to readable date
     */
    fun getFormattedDate(): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(logged)
            val readableFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
            readableFormat.format(date!!)
        } catch (e: Exception) {
            logged // Return original if parsing fails
        }
    }

    /**
     * Display helpers for UI
     */
    fun getDisplayTitle(): String = namaLoket.takeIf { it.isNotBlank() } ?: "PPID: $ppid"
    fun getDisplaySubtitle(): String = "Ref: $refNumber"
    fun getDisplayPhone(): String = formatPhoneNumber(nomorHP)

    private fun formatPhoneNumber(phone: String): String {
        return when {
            phone.startsWith("+62") -> phone
            phone.startsWith("08") -> "+62${phone.substring(1)}"
            phone.startsWith("62") -> "+$phone"
            phone.isNotBlank() -> phone
            else -> "No. HP tidak tersedia"
        }
    }

    /**
     * Navigation helper
     */
    fun getNavigationPpid(): String = ppid

    /**
     * Validation
     */
    fun hasValidData(): Boolean = ppid.isNotBlank() && refNumber.isNotBlank()

    /**
     * Search helper
     */
    fun matchesPpidSearch(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return ppid.lowercase().contains(lowerQuery) ||
                namaLoket.lowercase().contains(lowerQuery) ||
                refNumber.lowercase().contains(lowerQuery)
    }
}

/**
 * TransactionLog: Core data from /trx/ppid/{ppid}
 * Represents individual transaction entries
 */
data class TransactionLog(
    val tldRefnum: String,           // Transaction reference number
    val tldPan: String,              // PAN (card number, masked)
    val tldIdpel: String,            // Customer ID
    val tldAmount: Long,             // Transaction amount (positive/negative)
    val tldBalance: Long,            // Resulting balance
    val tldDate: String,             // Transaction timestamp
    val tldPpid: String              // PPID identifier
) {

    /**
     * Transaction type detection
     */
    fun isIncomingTransaction(): Boolean = tldAmount >= 0
    fun isOutgoingTransaction(): Boolean = tldAmount < 0

    /**
     * Format amount with proper sign and currency
     */
    fun getFormattedAmount(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0

        return if (tldAmount >= 0) {
            "+${numberFormat.format(tldAmount)}"
        } else {
            numberFormat.format(tldAmount)
        }
    }

    /**
     * Format balance
     */
    fun getFormattedBalance(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(tldBalance)
    }

    /**
     * Format transaction date
     */
    fun getFormattedDate(): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(tldDate)
            val readableFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
            readableFormat.format(date!!)
        } catch (e: Exception) {
            tldDate // Return original if parsing fails
        }
    }

    /**
     * Display helpers
     */
    fun getDisplayDescription(): String = "Ref: $tldRefnum • ID: $tldIdpel"
    fun getBalanceDisplayText(): String = "Saldo: ${getFormattedBalance()}"
    fun getMaskedPan(): String {
        return when {
            tldPan.length > 8 -> "${tldPan.take(4)}****${tldPan.takeLast(4)}"
            tldPan.isNotBlank() -> tldPan
            else -> "****"
        }
    }

    /**
     * Transaction type for UI styling
     */
    fun getTransactionType(): TransactionType = if (isIncomingTransaction()) {
        TransactionType.INCOMING
    } else {
        TransactionType.OUTGOING
    }

    /**
     * Validation
     */
    fun hasValidData(): Boolean = tldRefnum.isNotBlank() && tldPpid.isNotBlank()

    enum class TransactionType {
        INCOMING, OUTGOING
    }
}

// ============================================================================
// SUPPORTING MODELS
// ============================================================================

/**
 * LoketStatus: Status enum for profile status management
 * Based on PPID suffix detection (e.g., "PIDLKTD0025blok")
 */
enum class LoketStatus {
    NORMAL,    // Default status
    BLOCKED,   // Blocked (ppid ends with "blok")
    FLAGGED;   // Flagged for monitoring

    companion object {
        /**
         * Determine status from PPID
         * Block logic: PPID ends with "blok" = BLOCKED
         */
        fun fromPpid(ppid: String?): LoketStatus {
            return if (ppid?.endsWith("blok") == true) {
                BLOCKED
            } else {
                NORMAL
            }
        }

        fun fromString(status: String?): LoketStatus {
            return when (status?.uppercase()) {
                "BLOCKED", "DIBLOKIR" -> BLOCKED
                "FLAGGED", "DITANDAI" -> FLAGGED
                else -> NORMAL
            }
        }
    }
}

/**
 * Admin: User authentication model (keep existing)
 */
data class Admin(
    val name: String,
    val email: String,
    val token: String,
    val role: String = "admin"
) {
    fun isValidToken(): Boolean = token.isNotBlank() && token.length > 10
    fun getDisplayName(): String = if (name.isNotBlank()) name else email.substringBefore("@")
    fun hasValidCredentials(): Boolean = name.isNotBlank() && email.isNotBlank() && isValidToken()
}

// ============================================================================
// EXTENSION FUNCTIONS
// ============================================================================

/**
 * Receipt collection extensions
 */
fun List<Receipt>.getTotalAmount(): Long = sumOf { it.amount }
fun List<Receipt>.getValidReceipts(): List<Receipt> = filter { it.hasValidData() }
fun List<Receipt>.sortByAmountDescending(): List<Receipt> = sortedByDescending { it.amount }
fun List<Receipt>.sortByDate(): List<Receipt> = sortedBy { it.logged }
fun List<Receipt>.searchByPpid(query: String): List<Receipt> =
    filter { it.matchesPpidSearch(query) }

/**
 * TransactionLog collection extensions
 */
fun List<TransactionLog>.getTotalIncoming(): Long =
    filter { it.isIncomingTransaction() }.sumOf { it.tldAmount }

fun List<TransactionLog>.getTotalOutgoing(): Long =
    filter { it.isOutgoingTransaction() }.sumOf { kotlin.math.abs(it.tldAmount) }

fun List<TransactionLog>.getNetAmount(): Long = sumOf { it.tldAmount }
fun List<TransactionLog>.getLatestBalance(): Long = firstOrNull()?.tldBalance ?: 0L
fun List<TransactionLog>.sortByDateDescending(): List<TransactionLog> =
    sortedByDescending { it.tldDate }

fun List<TransactionLog>.getValidTransactions(): List<TransactionLog> =
    filter { it.hasValidData() }

// ============================================================================
// UTILITY HELPERS
// ============================================================================

/**
 * PPID validation and formatting utilities
 */
object PpidUtils {

    fun isValidPpid(ppid: String?): Boolean {
        return !ppid.isNullOrBlank() && ppid.length >= 5
    }

    fun extractCleanPpid(ppid: String): String {
        return ppid.removeSuffix("blok")
    }

    fun createBlockedPpid(ppid: String): String {
        val cleanPpid = extractCleanPpid(ppid)
        return "${cleanPpid}blok"
    }

    fun isBlockedPpid(ppid: String): Boolean {
        return ppid.endsWith("blok")
    }

    /**
     * PPID format examples for validation
     */
    fun getPpidFormatExamples(): List<String> {
        return listOf(
            "PIDLKTD0025",
            "PIDLKTD0025blok",
            "0000001",
            "PIDLKTD0014"
        )
    }

    /**
     * Validate PPID format against known patterns
     */
    fun validatePpidFormat(ppid: String): ValidationResult {
        return when {
            ppid.isBlank() -> ValidationResult(false, "PPID tidak boleh kosong")
            ppid.length < 5 -> ValidationResult(false, "PPID minimal 5 karakter")
            isValidPpidPattern(ppid) -> ValidationResult(true, "Format PPID valid")
            else -> ValidationResult(false, "Format PPID tidak dikenal")
        }
    }

    private fun isValidPpidPattern(ppid: String): Boolean {
        val patterns = listOf(
            "^PIDLKTD\\d+.*$".toRegex(),     // PIDLKTD0025, PIDLKTD0025blok
            "^\\d+$".toRegex(),              // 0000001
            "^[A-Z]{3,}\\d+.*$".toRegex()    // Generic pattern
        )
        return patterns.any { it.matches(ppid) }
    }

    data class ValidationResult(val isValid: Boolean, val message: String)
}

/**
 * Navigation argument helpers
 */
object NavigationUtils {

    fun createDetailBundle(ppid: String): android.os.Bundle {
        return android.os.Bundle().apply {
            putString("ppid", ppid)
        }
    }

    fun extractPpidFromBundle(bundle: android.os.Bundle?): String? {
        return bundle?.getString("ppid")
    }

    fun safeExtractPpid(bundle: android.os.Bundle?, fallback: String = ""): String {
        return extractPpidFromBundle(bundle) ?: fallback
    }
}
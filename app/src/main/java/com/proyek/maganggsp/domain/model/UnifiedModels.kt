// File: app/src/main/java/com/proyek/maganggsp/domain/model/UnifiedModels.kt - COMPLETE REFACTOR
package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * COMPLETE REFACTOR: Unified domain models sesuai prompt requirements
 * Eliminates scattered model classes, focuses on Receipt + TransactionLog
 */

// ============================================================================
// PRIMARY MODELS (sesuai prompt)
// ============================================================================

/**
 * Receipt: Core model for home screen and profile display
 * Maps dari /profiles/ppid/{ppid} API response
 */
data class Receipt(
    val refNumber: String,           // Reference untuk receipt ini
    val idPelanggan: String,         // Customer ID (PPID)
    val amount: Long,                // Transaction amount
    val logged: String,              // Timestamp ISO format
    val ppid: String,                // Primary identifier untuk navigation

    // Extended info untuk UI display
    val namaLoket: String = "",      // Nama loket untuk display
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
        return formatISODate(logged)
    }

    /**
     * Display helpers for UI
     */
    fun getDisplayTitle(): String = namaLoket.takeIf { it.isNotBlank() } ?: "Receipt $refNumber"
    fun getDisplaySubtitle(): String = "ID: $idPelanggan"
    fun getDisplayPhone(): String = formatPhoneNumber(nomorHP)

    /**
     * Navigation helper - PPID untuk navigate ke TransactionLog
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
 * TransactionLog: Core model for transaction detail screen
 * Maps dari /trx/ppid/{ppid} API response
 * NOTE: message field TIDAK ditampilkan sesuai prompt
 */
data class TransactionLog(
    val tldRefnum: String,           // Transaction reference number
    val tldPan: String,              // PAN (card number, masked)
    val tldIdpel: String,            // Customer ID
    val tldAmount: Long,             // Transaction amount (positive/negative)
    val tldBalance: Long,            // Resulting balance
    val tldDate: String,             // Transaction timestamp ISO format
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
    fun getFormattedDate(): String = formatISODate(tldDate)

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

/**
 * LoketStatus: Status management dengan block/unblock logic
 * Sesuai API PUT /profiles/ppid/{ppid} dengan {"mpPpid": "value"}
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
 * Admin: Keep existing for authentication
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
// UTILITY FUNCTIONS
// ============================================================================

/**
 * CONSOLIDATED: Phone number formatting
 */
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
 * CONSOLIDATED: ISO date formatting
 */
private fun formatISODate(dateString: String): String {
    return try {
        val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        isoFormat.timeZone = TimeZone.getTimeZone("UTC")
        val date = isoFormat.parse(dateString)
        val readableFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
        readableFormat.format(date!!)
    } catch (e: Exception) {
        dateString // Return original if parsing fails
    }
}

// ============================================================================
// COLLECTION EXTENSIONS
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
// BLOCK/UNBLOCK UTILITIES
// ============================================================================

/**
 * PPID Block/Unblock utilities sesuai API
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
     * Create block request body untuk API
     * PUT /profiles/ppid/{ppid} dengan {"mpPpid": "ppidblok"}
     */
    fun createBlockRequest(originalPpid: String): Map<String, String> {
        val blockedPpid = createBlockedPpid(originalPpid)
        return mapOf("mpPpid" to blockedPpid)
    }

    /**
     * Create unblock request body untuk API
     * PUT /profiles/ppid/{ppid} dengan {"mpPpid": "originalPpid"}
     */
    fun createUnblockRequest(blockedPpid: String): Map<String, String> {
        val originalPpid = extractCleanPpid(blockedPpid)
        return mapOf("mpPpid" to originalPpid)
    }

    /**
     * PPID validation dengan format checking
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

    data class ValidationResult(val isValid: Boolean, val message: String)
}

/**
 * Navigation utilities
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
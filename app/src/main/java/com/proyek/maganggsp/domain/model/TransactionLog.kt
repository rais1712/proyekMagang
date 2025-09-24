// File: app/src/main/java/com/proyek/maganggsp/domain/model/TransactionLog.kt
package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * MODULAR: TransactionLog domain model
 * Berdasarkan API /trx/ppid/{ppid} dan UI mockup receipt table
 * Shows: PLG000123, +Rp50.000/-Rp100.000, Saldo: Rp29.850.000, 15 Juli 2025
 */
data class TransactionLog(
    val tldRefnum: String,           // Transaction reference (PLG000123)
    val tldPan: String,              // PAN card number (masked)
    val tldIdpel: String,            // Customer ID
    val tldAmount: Long,             // Transaction amount (positive/negative)
    val tldBalance: Long,            // Resulting balance after transaction
    val tldDate: String,             // ISO timestamp
    val tldPpid: String              // Associated PPID
) {

    /**
     * Format amount with proper sign like UI mockup: +Rp50.000 or -Rp100.000
     */
    fun getFormattedAmount(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0

        return if (tldAmount >= 0) {
            "+${numberFormat.format(tldAmount)}"
        } else {
            numberFormat.format(tldAmount) // Already has minus sign
        }
    }

    /**
     * Format balance info like UI mockup: Saldo: Rp29.850.000
     */
    fun getBalanceDisplayText(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return "Saldo: ${numberFormat.format(tldBalance)}"
    }

    /**
     * Format date to readable Indonesian format: 15 Juli 2025
     */
    fun getFormattedDate(): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(tldDate)
            val readableFormat = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
            readableFormat.format(date!!)
        } catch (e: Exception) {
            tldDate // Return original if parsing fails
        }
    }

    /**
     * Get display description for transaction item
     */
    fun getDisplayDescription(): String {
        return "No. Ref: $tldRefnum"
    }

    /**
     * Check if transaction is incoming (positive amount)
     */
    fun isIncomingTransaction(): Boolean = tldAmount > 0

    /**
     * Check if transaction is outgoing (negative amount)
     */
    fun isOutgoingTransaction(): Boolean = tldAmount < 0

    /**
     * Validation
     */
    fun hasValidData(): Boolean = tldRefnum.isNotBlank() && tldPpid.isNotBlank()

    /**
     * Get transaction type for UI display
     */
    fun getTransactionType(): TransactionType {
        return when {
            tldAmount > 0 -> TransactionType.INCOMING
            tldAmount < 0 -> TransactionType.OUTGOING
            else -> TransactionType.ZERO
        }
    }

    enum class TransactionType {
        INCOMING,    // Green - dana masuk
        OUTGOING,    // Red - dana keluar
        ZERO         // Neutral - no change
    }
}
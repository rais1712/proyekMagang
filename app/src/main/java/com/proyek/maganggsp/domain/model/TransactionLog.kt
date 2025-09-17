// File: app/src/main/java/com/proyek/maganggsp/domain/model/TransactionLog.kt - SIMPLIFIED
package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * SIMPLIFIED: Transaction log model for detail screen
 * Focus on essential transaction data display
 */
data class TransactionLog(
    val tldRefnum: String,
    val tldPan: String,
    val tldIdpel: String,
    val tldAmount: Long,
    val tldBalance: Long,
    val tldDate: String,
    val tldPpid: String
) {

    /**
     * Transaction type detection
     */
    fun isIncomingTransaction(): Boolean = tldAmount >= 0
    fun isOutgoingTransaction(): Boolean = tldAmount < 0

    /**
     * Format amount dengan proper currency dan sign
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
     * Format date dalam bahasa Indonesia
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
    fun getDisplayDescription(): String = "Ref: $tldRefnum | ID: $tldIdpel"
    fun getBalanceDisplayText(): String = "Saldo: ${getFormattedBalance()}"
    fun getMaskedPan(): String {
        return when {
            tldPan.length > 8 -> "${tldPan.take(4)}****${tldPan.takeLast(4)}"
            tldPan.isNotBlank() -> tldPan
            else -> "****"
        }
    }

    /**
     * Validation
     */
    fun hasValidData(): Boolean = tldRefnum.isNotBlank() && tldIdpel.isNotBlank() && tldPpid.isNotBlank()
}


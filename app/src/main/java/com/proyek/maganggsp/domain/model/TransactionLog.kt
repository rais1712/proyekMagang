// File: app/src/main/java/com/proyek/maganggsp/domain/model/TransactionLogExtensions.kt - CREATED
package com.proyek.maganggsp.domain.model

import com.proyek.maganggsp.util.AppUtils

/**
 * CRITICAL FIX: Missing extension functions untuk TransactionLog
 * Referenced dalam TransactionLogAdapter dan ViewModels
 */

/**
 * Check if transaction is incoming (positive amount)
 */
fun TransactionLog.isIncomingTransaction(): Boolean = tldAmount > 0

/**
 * Check if transaction is outgoing (negative amount)
 */
fun TransactionLog.isOutgoingTransaction(): Boolean = tldAmount < 0

/**
 * Get formatted amount dengan proper currency dan sign
 */
fun TransactionLog.getFormattedAmount(): String {
    val sign = if (tldAmount >= 0) "+" else ""
    return "$sign${AppUtils.formatCurrency(kotlin.math.abs(tldAmount))}"
}

/**
 * Get formatted date dalam bahasa Indonesia
 */
fun TransactionLog.getFormattedDate(): String {
    return if (tldDate.isNotBlank()) {
        AppUtils.formatDate(tldDate)
    } else {
        "Tanggal tidak tersedia"
    }
}

/**
 * Get display description untuk transaction
 */
fun TransactionLog.getDisplayDescription(): String {
    return when {
        tldAmount > 0 -> "Top Up / Deposit"
        tldAmount < 0 -> "Pembayaran / Withdrawal"
        else -> "Informasi Saldo"
    }
}

/**
 * Get balance display text dengan currency formatting
 */
fun TransactionLog.getBalanceDisplayText(): String {
    return "Saldo: ${AppUtils.formatCurrency(tldBalance)}"
}

/**
 * Get transaction summary untuk display
 */
fun TransactionLog.getTransactionSummary(): String {
    val type = if (isIncomingTransaction()) "Masuk" else "Keluar"
    return "$type: ${getFormattedAmount()} | ${getBalanceDisplayText()}"
}

/**
 * Get PAN display dengan masking
 */
fun TransactionLog.getMaskedPan(): String {
    return when {
        tldPan.isNotBlank() && tldPan.length > 8 -> {
            "${tldPan.take(4)}****${tldPan.takeLast(4)}"
        }
        tldPan.isNotBlank() -> tldPan
        else -> "****"
    }
}

/**
 * Check if transaction is recent (last 24 hours)
 */
fun TransactionLog.isRecentTransaction(): Boolean {
    return try {
        val transactionTime = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            java.util.Locale.getDefault()
        ).parse(tldDate)?.time ?: 0L

        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - transactionTime
        val oneDayInMillis = 24 * 60 * 60 * 1000L

        timeDifference < oneDayInMillis
    } catch (e: Exception) {
        false
    }
}

/**
 * Get transaction age description
 */
fun TransactionLog.getTransactionAge(): String {
    return try {
        val transactionTime = java.text.SimpleDateFormat(
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            java.util.Locale.getDefault()
        ).parse(tldDate)?.time ?: return "Waktu tidak diketahui"

        val currentTime = System.currentTimeMillis()
        val timeDifference = currentTime - transactionTime

        when {
            timeDifference < 60 * 1000L -> "Baru saja"
            timeDifference < 60 * 60 * 1000L -> "${timeDifference / (60 * 1000L)} menit lalu"
            timeDifference < 24 * 60 * 60 * 1000L -> "${timeDifference / (60 * 60 * 1000L)} jam lalu"
            timeDifference < 7 * 24 * 60 * 60 * 1000L -> "${timeDifference / (24 * 60 * 60 * 1000L)} hari lalu"
            else -> "Lebih dari seminggu lalu"
        }
    } catch (e: Exception) {
        "Waktu tidak diketahui"
    }
}

/**
 * Create formatted transaction card data untuk UI
 */
data class TransactionCardData(
    val title: String,
    val subtitle: String,
    val amount: String,
    val balance: String,
    val timestamp: String,
    val isIncoming: Boolean,
    val maskedPan: String
)

/**
 * Convert TransactionLog to TransactionCardData untuk UI display
 */
fun TransactionLog.toCardData(): TransactionCardData {
    return TransactionCardData(
        title = getDisplayDescription(),
        subtitle = "ID: ${tldIdpel}",
        amount = getFormattedAmount(),
        balance = getBalanceDisplayText(),
        timestamp = getFormattedDate(),
        isIncoming = isIncomingTransaction(),
        maskedPan = getMaskedPan()
    )
}
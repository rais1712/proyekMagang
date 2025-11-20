// File: app/src/main/java/com/proyek/maganggsp/data/dto/TransactionDtos.kt
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName
import com.proyek.maganggsp.domain.model.TransactionLog

/**
 * MODULAR: Transaction API DTOs and mapping functions
 * Based on actual HTTP request: GET /trx/ppid/{ppid}
 */

/**
 * TransactionResponse: Response from GET /trx/ppid/{ppid}
 * Maps to TransactionLog domain model
 */
data class TransactionResponse(
    @SerializedName("tldRefnum")
    val tldRefnum: String?,

    @SerializedName("tldPan")
    val tldPan: String?,

    @SerializedName("tldIdpel")
    val tldIdpel: String?,

    @SerializedName("tldAmount")
    val tldAmount: Long?,

    @SerializedName("tldBalance")
    val tldBalance: Long?,

    @SerializedName("tldDate")
    val tldDate: String?,

    @SerializedName("tldPpid")
    val tldPpid: String?
)

/**
 * TransactionResponse to TransactionLog mapping
 */
fun TransactionResponse.toTransactionLog(): TransactionLog {
    return TransactionLog(
        tldRefnum = tldRefnum ?: "",
        tldPan = tldPan ?: "",
        tldIdpel = tldIdpel ?: "",
        tldAmount = tldAmount ?: 0L,
        tldBalance = tldBalance ?: 0L,
        tldDate = tldDate ?: getCurrentTimestamp(),
        tldPpid = tldPpid ?: ""
    )
}

/**
 * Collection mapping extensions
 */
fun List<TransactionResponse>.toTransactionLogs(): List<TransactionLog> =
    mapNotNull { response ->
        if (response.tldRefnum?.isNotBlank() == true) {
            response.toTransactionLog()
        } else {
            null // Skip invalid entries
        }
    }

/**
 * Get current timestamp in ISO format
 */
private fun getCurrentTimestamp(): String {
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
    formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return formatter.format(java.util.Date())
}

/**
 * Validation helpers
 */
fun TransactionResponse.isValid(): Boolean {
    return !tldRefnum.isNullOrBlank() && !tldPpid.isNullOrBlank()
}

fun List<TransactionResponse>.filterValid(): List<TransactionResponse> =
    filter { it.isValid() }

/**
 * Create placeholder transaction responses for development
 */
fun createPlaceholderTransactionResponses(ppid: String, count: Int = 5): List<TransactionResponse> {
    val transactions = mutableListOf<TransactionResponse>()
    var balance = 1000000L

    repeat(count) { index ->
        val amount = if (index % 3 == 0) {
            -(10000..50000L).random() // Outgoing
        } else {
            (25000..100000L).random() // Incoming
        }
        balance += amount

        transactions.add(
            TransactionResponse(
                tldRefnum = "PLG${String.format("%06d", index + 100)}", // PLG000123 format
                tldPan = "1234****5678",
                tldIdpel = ppid,
                tldAmount = amount,
                tldBalance = balance,
                tldDate = getCurrentTimestamp(),
                tldPpid = ppid
            )
        )
    }

    return transactions.reversed() // Most recent first
}
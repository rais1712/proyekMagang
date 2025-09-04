// File: app/src/main/java/com/proyek/maganggsp/domain/model/TransactionLog.kt - ENHANCED
package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * ✅ PHASE 1: Enhanced TransactionLog model with utility functions
 * This represents detailed transaction logs from /trx/ppid/{ppid}
 * NOTE: message field is NOT displayed as per requirements
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

    // Utility functions for TransactionLog model
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

    fun getFormattedBalance(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(tldBalance)
    }

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

    fun isIncomingTransaction(): Boolean = tldAmount >= 0

    fun isOutgoingTransaction(): Boolean = tldAmount < 0

    fun getTransactionType(): TransactionType = if (isIncomingTransaction()) {
        TransactionType.INCOMING
    } else {
        TransactionType.OUTGOING
    }

    fun getDisplayDescription(): String = "Ref: $tldRefnum | ID: $tldIdpel"

    fun getBalanceDisplayText(): String = "Saldo: ${getFormattedBalance()}"

    fun hasValidData(): Boolean = tldRefnum.isNotBlank() && tldIdpel.isNotBlank() && tldPpid.isNotBlank()

    fun toDebugString(): String = "TransactionLog(ref='$tldRefnum', amount=$tldAmount, balance=$tldBalance)"

    // Enum for transaction types
    enum class TransactionType {
        INCOMING, OUTGOING
    }
}
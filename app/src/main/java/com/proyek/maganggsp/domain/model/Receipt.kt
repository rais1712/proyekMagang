// File: app/src/main/java/com/proyek/maganggsp/domain/model/Receipt.kt - ENHANCED
package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.util.*

/**
 * ✅ PHASE 1: Enhanced Receipt model with utility functions
 * This represents receipt data from /profiles/ppid/{ppid}
 * Action: navigate to log detail when clicked
 */
data class Receipt(
    val refNumber: String,
    val idPelanggan: String,
    val amount: Long,
    val logged: String
) {

    // Utility functions for Receipt model
    fun getFormattedAmount(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(amount)
    }

    fun getDisplayTitle(): String = "Receipt #$refNumber"

    fun getDisplaySubtitle(): String = "ID: $idPelanggan"

    fun hasValidData(): Boolean = refNumber.isNotBlank() && idPelanggan.isNotBlank()

    fun isLargeAmount(): Boolean = amount >= 1_000_000 // 1 million rupiah

    fun getLoggedDisplayText(): String = when {
        logged.isBlank() || logged == "-" -> "No timestamp available"
        else -> "Logged: $logged"
    }

    fun toDebugString(): String = "Receipt(ref='$refNumber', id='$idPelanggan', amount=$amount)"
}
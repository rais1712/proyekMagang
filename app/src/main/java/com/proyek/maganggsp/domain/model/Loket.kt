// File: app/src/main/java/com/proyek/maganggsp/domain/model/Loket.kt
package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.util.*

/**
 * ENHANCED: Loket domain model dengan comprehensive profile management
 * Maps dari ProfileResponse (/profiles/ppid/{ppid}) dengan additional UI enhancements
 */
data class Loket(
    val ppid: String,
    val namaLoket: String,
    val nomorHP: String,
    val alamat: String,
    val email: String,
    val status: LoketStatus,
    val saldoTerakhir: Long = 0L,
    val tanggalAkses: String = "",
    val receipts: List<Receipt> = emptyList()
) {
    fun getFormattedSaldo(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(saldoTerakhir)
    }

    fun getDisplayTitle(): String = "$namaLoket ($ppid)"

    fun hasValidData(): Boolean = ppid.isNotBlank() && namaLoket.isNotBlank()

    fun getStatusDisplayText(): String = when (status) {
        LoketStatus.NORMAL -> "Normal"
        LoketStatus.BLOCKED -> "Diblokir"
        LoketStatus.FLAGGED -> "Ditandai"
    }

    fun isBlocked(): Boolean = status == LoketStatus.BLOCKED
    fun isFlagged(): Boolean = status == LoketStatus.FLAGGED
}

/**
 * ENHANCED: Loket status dengan flagged option untuk monitoring
 */
enum class LoketStatus {
    NORMAL,
    BLOCKED,
    FLAGGED
}



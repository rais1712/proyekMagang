// File: app/src/main/java/com/proyek/maganggsp/domain/model/Loket.kt

package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.util.Locale

/**
 * BRIDGE MODEL: For DetailLoket screen compatibility
 * Maps between new Receipt/TransactionLog structure and existing UI
 */
data class Loket(
    val ppid: String,
    val namaLoket: String,
    val nomorHP: String,
    val alamat: String,
    val email: String,
    val status: LoketStatus,
    val saldoTerakhir: Long = 0L,
    val tanggalAkses: String = ""
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
        LoketStatus.SUSPENDED -> "Ditangguhkan"
        LoketStatus.UNKNOWN -> "Tidak Diketahui"
    }

    fun getStatusColor(): String = when (status) {
        LoketStatus.NORMAL -> "#2E7D32"
        LoketStatus.BLOCKED -> "#D32F2F"
        LoketStatus.FLAGGED -> "#F57C00"
        LoketStatus.SUSPENDED -> "#F57C00"
        LoketStatus.UNKNOWN -> "#757575"
    }

    fun getStatusBackgroundColor(): String = when (status) {
        LoketStatus.NORMAL -> "#E8F5E8"
        LoketStatus.BLOCKED -> "#FFEBEE"
        LoketStatus.FLAGGED -> "#FFF3E0"
        LoketStatus.SUSPENDED -> "#FFF3E0"
        LoketStatus.UNKNOWN -> "#F5F5F5"
    }

    // Status check functions
    fun isBlocked(): Boolean = status == LoketStatus.BLOCKED
    fun isFlagged(): Boolean = status == LoketStatus.FLAGGED
    fun isSuspended(): Boolean = status == LoketStatus.SUSPENDED
    fun isNormal(): Boolean = status == LoketStatus.NORMAL
    fun canBeUpdated(): Boolean = status != LoketStatus.FLAGGED

    // Block/unblock API logic
    fun getOriginalPpid(): String = ppid.removeSuffix("blok")
    fun getBlockedPpid(): String = if (ppid.endsWith("blok")) ppid else "${ppid}blok"

    // Helper methods for SearchAdapter
    fun getDisplayPhone(): String = nomorHP.ifBlank { "-" }
    fun getDisplayPpid(): String = ppid.ifBlank { "-" }
    fun getDisplayAddress(): String = alamat.ifBlank { "-" }
    fun getDisplayEmail(): String = email.ifBlank { "-" }

    // Conversion helper for API
    fun toApiStatus(): String = LoketStatus.toApiString(status)
}

// File: app/src/main/java/com/proyek/maganggsp/domain/model/Loket.kt - BRIDGE MODEL
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
    }

    fun isBlocked(): Boolean = status == LoketStatus.BLOCKED
    fun isFlagged(): Boolean = status == LoketStatus.FLAGGED
    fun isNormal(): Boolean = status == LoketStatus.NORMAL

    // Block/unblock API logic
    fun getOriginalPpid(): String = ppid.removeSuffix("blok")
    fun getBlockedPpid(): String = if (ppid.endsWith("blok")) ppid else "${ppid}blok"
}

/**
 * Status enum dengan API logic
 */
enum class LoketStatus {
    NORMAL,
    BLOCKED,
    FLAGGED;

    companion object {
        fun fromPpid(ppid: String?): LoketStatus {
            return if (ppid?.endsWith("blok") == true) {
                BLOCKED
            } else {
                NORMAL
            }
        }
    }
}
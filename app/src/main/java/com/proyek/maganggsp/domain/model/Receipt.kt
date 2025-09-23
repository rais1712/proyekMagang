// File: app/src/main/java/com/proyek/maganggsp/domain/model/Receipt.kt
package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * MODULAR: Receipt domain model
 * Based on UI mockup showing receipt entries with:
 * - PLG000123 (ref number)
 * - Amount with +/- indication (+Rp50.000, -Rp100.000)
 * - Saldo info (Saldo: Rp29.850.000)
 * - Date (15 Juli 2025)
 */
data class Receipt(
    val refNumber: String,           // PLG000123
    val noPelanggan: String,         // Customer number
    val tipe: String,                // Pembayaran, Inquiry, etc
    val jumlah: Long,                // Transaction amount (positive/negative)
    val status: String,              // Berhasil, Gagal
    val waktu: String,               // ISO timestamp
    val saldoSetelah: Long = 0L,     // Balance after transaction
    val ppid: String = "",           // Associated PPID for navigation

    // Additional metadata
    val keterangan: String = ""      // Additional description if needed
) {

    /**
     * Format jumlah with proper sign and currency
     */
    fun getFormattedJumlah(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0

        return if (jumlah >= 0) {
            "+${numberFormat.format(jumlah)}"
        } else {
            numberFormat.format(jumlah)
        }
    }

    /**
     * Format saldo after transaction
     */
    fun getFormattedSaldo(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return "Saldo: ${numberFormat.format(saldoSetelah)}"
    }

    /**
     * Format waktu to readable date
     */
    fun getFormattedWaktu(): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(waktu)
            val readableFormat = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
            readableFormat.format(date!!)
        } catch (e: Exception) {
            waktu // Return original if parsing fails
        }
    }

    /**
     * Get receipt type icon/color based on tipe and status
     */
    fun getReceiptTypeIndicator(): ReceiptTypeIndicator {
        return when {
            status == "Gagal" -> ReceiptTypeIndicator.FAILED
            jumlah > 0 -> ReceiptTypeIndicator.INCOMING
            jumlah < 0 -> ReceiptTypeIndicator.OUTGOING
            tipe == "Inquiry" -> ReceiptTypeIndicator.INQUIRY
            else -> ReceiptTypeIndicator.NEUTRAL
        }
    }

    /**
     * Check if receipt is successful
     */
    fun isSuccessful(): Boolean = status.equals("Berhasil", ignoreCase = true)

    /**
     * Check if receipt is failed
     */
    fun isFailed(): Boolean = status.equals("Gagal", ignoreCase = true)

    /**
     * Get display subtitle for receipt card
     */
    fun getDisplaySubtitle(): String {
        return "No. Ref: $refNumber • $noPelanggan"
    }

    /**
     * Validation
     */
    fun isValid(): Boolean = refNumber.isNotBlank() && noPelanggan.isNotBlank()

    enum class ReceiptTypeIndicator {
        INCOMING,    // Green - positive amount
        OUTGOING,    // Red - negative amount
        INQUIRY,     // Blue - inquiry type
        FAILED,      // Red - failed status
        NEUTRAL      // Gray - default
    }
}
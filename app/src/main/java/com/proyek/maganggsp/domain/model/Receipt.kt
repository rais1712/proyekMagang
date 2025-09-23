// File: app/src/main/java/com/proyek/maganggsp/domain/model/Receipt.kt
package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * MODULAR: Receipt domain model based on UI mockup and table data
 * Berdasarkan tabel: REF_NUM, NO_PELANGGAN, TIPE, JUMLAH, STATUS, WAKTU, AKSI
 * UI Cards: Shows nama, ppid (#101010), and transaction details
 */
data class Receipt(
    val refNumber: String,           // REF_NUM dari tabel
    val noPelanggan: String,         // NO_PELANGGAN dari tabel
    val tipe: String,                // TIPE (Pembayaran, Inquiry)
    val jumlah: Long,                // JUMLAH (Rp 150.000, dll)
    val status: String,              // STATUS (Berhasil, Gagal)
    val waktu: String,               // WAKTU (ISO timestamp)
    val ppid: String,                // PPID untuk navigation

    // UI Display fields (dari /profiles/ppid/{ppid} response)
    val namaLoket: String = "",      // Nama untuk card display
    val nomorHP: String = "",        // Phone untuk subtitle
    val email: String = "",          // Email info
    val alamat: String = ""          // Address info
) {

    /**
     * Format jumlah sesuai tabel: +Rp150.000 atau -Rp100.000
     */
    fun getFormattedJumlah(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0

        return if (jumlah >= 0) {
            "+${numberFormat.format(jumlah)}"
        } else {
            numberFormat.format(jumlah) // Already has minus sign
        }
    }

    /**
     * Format waktu to readable Indonesian date
     */
    fun getFormattedWaktu(): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(waktu)
            val readableFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))
            readableFormat.format(date!!)
        } catch (e: Exception) {
            waktu // Return original if parsing fails
        }
    }

    /**
     * Display title untuk card sesuai UI mockup
     * UI Shows: "Loket Fatih"
     */
    fun getDisplayTitle(): String {
        return namaLoket.takeIf { it.isNotBlank() } ?: "Receipt $refNumber"
    }

    /**
     * Display phone untuk card subtitle
     * UI Shows: "+628123456790"
     */
    fun getDisplayPhone(): String {
        return when {
            nomorHP.startsWith("+62") -> nomorHP
            nomorHP.startsWith("08") -> "+62${nomorHP.substring(1)}"
            nomorHP.startsWith("62") -> "+$nomorHP"
            nomorHP.isNotBlank() -> nomorHP
            else -> "No. HP tidak tersedia"
        }
    }

    /**
     * Get PPID display untuk card
     * UI Shows: "#101010"
     */
    fun getDisplayPpid(): String {
        return "#$ppid"
    }

    /**
     * Subtitle untuk search results
     * Format: "No. Ref: REF20250405001 • PLG000123"
     */
    fun getDisplaySubtitle(): String {
        return "No. Ref: $refNumber • $noPelanggan"
    }

    /**
     * Check if receipt is successful (untuk UI color/icon)
     */
    fun isSuccessful(): Boolean = status.equals("Berhasil", ignoreCase = true)

    /**
     * Check if receipt is failed (untuk UI color/icon)
     */
    fun isFailed(): Boolean = status.equals("Gagal", ignoreCase = true)

    /**
     * Get receipt type indicator untuk UI styling
     */
    fun getReceiptTypeIndicator(): ReceiptTypeIndicator {
        return when {
            isFailed() -> ReceiptTypeIndicator.FAILED
            jumlah > 0 -> ReceiptTypeIndicator.INCOMING
            jumlah < 0 -> ReceiptTypeIndicator.OUTGOING
            tipe.contains("Inquiry", ignoreCase = true) -> ReceiptTypeIndicator.INQUIRY
            else -> ReceiptTypeIndicator.NEUTRAL
        }
    }

    /**
     * Validation
     */
    fun hasValidData(): Boolean = refNumber.isNotBlank() && noPelanggan.isNotBlank()

    /**
     * Receipt type indicators untuk UI
     */
    enum class ReceiptTypeIndicator {
        INCOMING,    // Green - positive amount
        OUTGOING,    // Red - negative amount
        INQUIRY,     // Blue - inquiry type
        FAILED,      // Red - failed status
        NEUTRAL      // Gray - default
    }
}
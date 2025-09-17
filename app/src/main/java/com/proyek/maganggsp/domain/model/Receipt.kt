// File: app/src/main/java/com/proyek/maganggsp/domain/model/Receipt.kt - UNIFIED FINAL
package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * FINAL: Primary domain model for home screen data
 * Replaces Loket-focused approach with Receipt-centric design
 */
data class Receipt(
    val refNumber: String,
    val idPelanggan: String,
    val amount: Long,
    val logged: String,
    val ppid: String = idPelanggan, // Map to customer ID for navigation
    val tipeTransaksi: String = "Receipt",

    // Extended fields for UI display
    val namaLoket: String = "",
    val nomorHP: String = "",
    val email: String = "",
    val alamat: String = "",
    val saldoTerakhir: Long = amount,
    val tanggalAkses: String = logged
) {

    /**
     * Format amount dengan proper currency formatting
     */
    fun getFormattedAmount(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return if (amount >= 0) {
            numberFormat.format(amount)
        } else {
            numberFormat.format(amount)
        }
    }

    /**
     * Format date untuk display
     */
    fun getFormattedDate(): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(logged)

            val readableFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
            readableFormat.format(date!!)
        } catch (e: Exception) {
            logged // Return original if parsing fails
        }
    }

    /**
     * Display helpers untuk HomeFragment
     */
    fun getDisplayTitle(): String = namaLoket.takeIf { it.isNotBlank() } ?: "Receipt $refNumber"
    fun getDisplaySubtitle(): String = "ID: $idPelanggan"
    fun getDisplayPhone(): String = formatPhoneNumber(nomorHP)

    private fun formatPhoneNumber(phone: String): String {
        return when {
            phone.startsWith("+62") -> phone
            phone.startsWith("08") -> "+62${phone.substring(1)}"
            phone.startsWith("62") -> "+$phone"
            phone.isNotBlank() -> phone
            else -> "No. HP tidak tersedia"
        }
    }

    /**
     * Navigation helper - get PPID for detail navigation
     */
    fun getNavigationPpid(): String = ppid.takeIf { it.isNotBlank() } ?: idPelanggan

    /**
     * Validation
     */
    fun hasValidData(): Boolean = refNumber.isNotBlank() && idPelanggan.isNotBlank()

    /**
     * Search matching for PPID-based search
     */
    fun matchesPpidSearch(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return ppid.lowercase().contains(lowerQuery) ||
                idPelanggan.lowercase().contains(lowerQuery) ||
                namaLoket.lowercase().contains(lowerQuery)
    }
}

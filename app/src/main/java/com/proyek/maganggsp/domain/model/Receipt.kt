// File: app/src/main/java/com/proyek/maganggsp/domain/model/Receipt.kt - ENHANCED
package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * ENHANCED: Receipt model with better formatting and PPID reference
 * This represents individual transactions/receipts within a loket
 */
data class Receipt(
    val refNumber: String,
    val idPelanggan: String,
    val tanggal: String,
    val mutasi: Long,
    val totalSaldo: Long,
    val ppid: String, // Reference to parent loket
    val tipeTransaksi: String = "Receipt" // For future transaction type handling
) {

    fun getFormattedMutasi(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0

        return if (mutasi >= 0) {
            "+${numberFormat.format(mutasi)}"
        } else {
            numberFormat.format(mutasi)
        }
    }

    fun getFormattedSaldo(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(totalSaldo)
    }

    fun getFormattedTanggal(): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(tanggal)

            val readableFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
            readableFormat.format(date!!)
        } catch (e: Exception) {
            tanggal
        }
    }

    fun isIncomingTransaction(): Boolean = mutasi >= 0
    fun isOutgoingTransaction(): Boolean = mutasi < 0

    fun getDisplayTitle(): String = "Receipt #$refNumber"
    fun getDisplaySubtitle(): String = "ID: $idPelanggan"

    fun hasValidData(): Boolean = refNumber.isNotBlank() && idPelanggan.isNotBlank()
}
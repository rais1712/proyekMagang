// File: app/src/main/java/com/proyek/maganggsp/domain/model/Receipt.kt - FIXED COMPLETE
package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * FIXED: Complete Receipt model sesuai dengan API response structure
 * Contains all required fields untuk proper data flow
 */
data class Receipt(
    val refNumber: String,
    val idPelanggan: String,
    val tanggal: String,
    val mutasi: Long,
    val totalSaldo: Long,
    val ppid: String,
    val tipeTransaksi: String = "Receipt"
) {

    // BACKWARD COMPATIBILITY: Keep existing properties for legacy code
    val amount: Long get() = mutasi
    val logged: String get() = tanggal

    // Utility functions untuk Receipt model
    fun getFormattedAmount(): String {
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

    fun getFormattedDate(): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(tanggal)

            val readableFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
            readableFormat.format(date!!)
        } catch (e: Exception) {
            tanggal // Return original if parsing fails
        }
    }

    fun isIncomingTransaction(): Boolean = mutasi >= 0

    fun isOutgoingTransaction(): Boolean = mutasi < 0

    fun getDisplayDescription(): String = "Ref: $refNumber | ID: $idPelanggan"

    fun getSaldoDisplayText(): String = "Saldo: ${getFormattedSaldo()}"

    fun hasValidData(): Boolean = refNumber.isNotBlank() && idPelanggan.isNotBlank() && ppid.isNotBlank()

    fun toDebugString(): String = "Receipt(ref='$refNumber', amount=$mutasi, saldo=$totalSaldo, ppid='$ppid')"
}
// File: app/src/main/java/com/proyek/maganggsp/domain/model/Receipt.kt - UNIFIED SINGLE SOURCE
package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * UNIFIED: Single Receipt domain model - eliminates all confusion
 * Compatible dengan TransactionLogViewModel dan semua existing usage
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

    // BACKWARD COMPATIBILITY: Legacy properties untuk existing code
    val amount: Long get() = mutasi
    val logged: String get() = tanggal

    /**
     * Format amount dengan proper currency formatting
     */
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

    /**
     * Format saldo dengan currency
     */
    fun getFormattedSaldo(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(totalSaldo)
    }

    /**
     * Format date untuk display
     */
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

    /**
     * Transaction type helpers
     */
    fun isIncomingTransaction(): Boolean = mutasi >= 0
    fun isOutgoingTransaction(): Boolean = mutasi < 0

    /**
     * Display helpers
     */
    fun getDisplayDescription(): String = "Ref: $refNumber | ID: $idPelanggan"
    fun getSaldoDisplayText(): String = "Saldo: ${getFormattedSaldo()}"

    /**
     * Validation
     */
    fun hasValidData(): Boolean = refNumber.isNotBlank() && idPelanggan.isNotBlank() && ppid.isNotBlank()

    companion object {
        /**
         * Create placeholder untuk testing
         */
        fun createPlaceholder(ppid: String): Receipt {
            return Receipt(
                refNumber = "REF-PLACEHOLDER-${System.currentTimeMillis().toString().takeLast(4)}",
                idPelanggan = ppid,
                tanggal = "2024-01-15T10:30:00.000Z",
                mutasi = (50000..500000L).random(),
                totalSaldo = (500000..2000000L).random(),
                ppid = ppid
            )
        }
    }
}
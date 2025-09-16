// File: app/src/main/java/com/proyek/maganggsp/domain/model/Receipt.kt - CREATED
package com.proyek.maganggsp.domain.model

import com.proyek.maganggsp.util.AppUtils

/**
 * CRITICAL FIX: Receipt domain model untuk TransactionLogViewModel compatibility
 * Maps to profile response data dengan receipt information
 */
data class Receipt(
    val refNumber: String,
    val idPelanggan: String,
    val tanggal: String = "",
    val mutasi: Long = 0L,
    val totalSaldo: Long = 0L,
    val ppid: String,

    // Compatibility aliases untuk existing code
    val amount: Long = mutasi,
    val logged: String = tanggal,
    val tipeTransaksi: String = "Receipt"
) {

    /**
     * Display formatted amount dengan proper currency
     */
    fun getFormattedAmount(): String {
        val sign = if (mutasi >= 0) "+" else ""
        return "$sign${AppUtils.formatCurrency(mutasi)}"
    }

    /**
     * Display formatted date dalam bahasa Indonesia
     */
    fun getFormattedDate(): String {
        return if (tanggal.isNotBlank()) {
            AppUtils.formatDate(tanggal)
        } else {
            "Tanggal tidak tersedia"
        }
    }

    /**
     * Get transaction type description
     */
    fun getTransactionTypeDescription(): String {
        return when {
            mutasi > 0 -> "Penambahan Saldo"
            mutasi < 0 -> "Pengurangan Saldo"
            else -> "Info Saldo"
        }
    }

    /**
     * Check if this is a positive transaction
     */
    fun isPositiveTransaction(): Boolean = mutasi > 0

    /**
     * Get display summary untuk UI
     */
    fun getDisplaySummary(): String {
        return "${getTransactionTypeDescription()}: ${getFormattedAmount()}"
    }
}

/**
 * EXTENSION: Convert from API response to Receipt
 */
fun com.proyek.maganggsp.data.dto.ReceiptResponse.toDomain(): Receipt {
    return Receipt(
        refNumber = refNumber ?: "REF-UNKNOWN",
        idPelanggan = idPelanggan ?: "",
        tanggal = tanggal ?: "",
        mutasi = mutasi ?: 0L,
        totalSaldo = totalSaldo ?: 0L,
        ppid = ppid ?: ""
    )
}

/**
 * HELPER: Create placeholder receipt untuk testing
 */
fun Receipt.Companion.createPlaceholder(ppid: String): Receipt {
    return Receipt(
        refNumber = "REF-PLACEHOLDER-${System.currentTimeMillis().toString().takeLast(4)}",
        idPelanggan = ppid,
        tanggal = "2024-01-15T10:30:00.000Z",
        mutasi = (50000..500000L).random(),
        totalSaldo = (500000..2000000L).random(),
        ppid = ppid
    )
}

// Companion object untuk static functions
companion object Receipt
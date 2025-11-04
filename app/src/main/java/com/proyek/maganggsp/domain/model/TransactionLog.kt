// File: app/src/main/java/com/proyek/maganggsp/domain/model/TransactionLog.kt
package com.proyek.maganggsp.domain.model

/**
 * Domain model untuk Transaction Log
 * Sesuai dengan API endpoint: GET /trx/ppid/{ppid}
 */
data class TransactionLog(
    val id: String,
    val refNumber: String,
    val noPelanggan: String,
    val amount: Long,
    val type: String, // "CREDIT" atau "DEBIT"
    val timestamp: String,
    val description: String,
    val status: String,
    val ppid: String,
    val saldo: Long = 0L
) {
    /**
     * Helper function untuk format amount sesuai UI mockup
     * +Rp50.000 atau -Rp100.000
     */
    fun getFormattedAmount(): String {
        val prefix = if (type == "CREDIT") "+" else "-"
        val formatted = java.text.DecimalFormat("#,###").format(amount)
        return "${prefix}Rp${formatted}"
    }

    /**
     * Helper function untuk mendapatkan warna berdasarkan type
     */
    fun getAmountColor(): String {
        return if (type == "CREDIT") "#2E7D32" else "#D32F2F" // green atau red
    }
    
    /**
     * Helper functions untuk filter transaksi
     */
    fun isIncomingTransaction(): Boolean = type == "CREDIT" || amount > 0
    
    fun isOutgoingTransaction(): Boolean = type == "DEBIT" || amount < 0
    
    /**
     * Helper function untuk validasi data
     */
    fun hasValidData(): Boolean {
        return refNumber.isNotBlank() && noPelanggan.isNotBlank() && ppid.isNotBlank()
    }
    
    /**
     * Alias properties untuk kompatibilitas dengan kode lama
     */
    val tldAmount: Long get() = amount
    val tldRefnum: String get() = refNumber
    val tldPan: String get() = noPelanggan
    val tldIdpel: String get() = noPelanggan
    val tldBalance: Long get() = saldo
    val tldDate: String get() = timestamp
    val tldPpid: String get() = ppid
    
    /**
     * Helper methods untuk display
     */
    fun getDisplayDescription(): String = description.ifBlank { "Transaksi $refNumber" }
    
    fun getBalanceDisplayText(): String {
        val formatted = java.text.DecimalFormat("#,###").format(saldo)
        return "Rp${formatted}"
    }
}

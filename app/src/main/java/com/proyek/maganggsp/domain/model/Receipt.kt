// File: app/src/main/java/com/proyek/maganggsp/domain/model/Receipt.kt
package com.proyek.maganggsp.domain.model

/**
 * Domain model untuk Receipt
 * Sesuai dengan API response dari GET /profiles/ppid/{ppid}
 * dan UI mockup yang diberikan
 */
data class Receipt(
    val refNumber: String,
    val noPelanggan: String,      // ✅ PAKAI INI (bukan idPelanggan)
    val tipe: String,
    val jumlah: Long,             // ✅ PAKAI INI (bukan mutasi)
    val status: String,
    val waktu: String,            // ✅ PAKAI INI (bukan tanggal)
    val ppid: String,

    // Profile info (dari API /profiles/ppid/{ppid})
    val namaLoket: String = "",
    val nomorHP: String = "",
    val email: String = "",
    val alamat: String = "",
    val loketStatus: LoketStatus = LoketStatus.NORMAL
) {
    /**
     * Helper function untuk format jumlah sesuai UI
     * PLG000123: +Rp50.000
     */
    fun getFormattedAmount(): String {
        val prefix = if (jumlah >= 0) "+" else ""
        val formatted = java.text.DecimalFormat("#,###").format(kotlin.math.abs(jumlah))
        return "${prefix}Rp${formatted}"
    }

    /**
     * Helper function untuk display receipt
     * Format: "PLG000123: +Rp50.000"
     */
    fun getDisplayText(): String {
        return "$noPelanggan: ${getFormattedAmount()}"
    }
}

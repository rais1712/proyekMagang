// File: app/src/main/java/com/proyek/maganggsp/domain/model/Mutasi.kt
package com.proyek.maganggsp.domain.model

/**
 * Merepresentasikan satu riwayat transaksi (mutasi) dari sebuah loket.
 */
data class Mutasi(
    val id: String,
    val description: String,
    val amount: Long,
    val type: String, // "IN" untuk masuk, "OUT" untuk keluar
    val balanceAfter: Long,
    val reference: String,
    val timestamp: String,
    var isFlagged: Boolean = false // Status apakah transaksi ini ditandai atau tidak
)
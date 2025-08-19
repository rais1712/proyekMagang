package com.proyek.maganggsp.domain.model

/**
 * FIXED: Updated Mutasi model untuk match dengan MutasiAdapter requirements
 * Ditambahkan fields yang dibutuhkan adapter + backward compatibility
 */
data class Mutasi(
    // Original fields (backward compatibility)
    val tanggal: String,
    val nomorReferensi: String,
    val nominalTransaksi: Long,
    val sisaSaldo: Long,

    // New fields untuk adapter compatibility
    val id: String = nomorReferensi, // Use nomorReferensi sebagai unique ID
    val timestamp: String = tanggal, // Alias untuk tanggal
    val reference: String = nomorReferensi, // Alias untuk nomorReferensi
    val amount: Long = nominalTransaksi, // Alias untuk nominalTransaksi
    val balanceAfter: Long = sisaSaldo, // Alias untuk sisaSaldo
    val type: String = if (nominalTransaksi >= 0) "IN" else "OUT" // Derived dari nominal
)
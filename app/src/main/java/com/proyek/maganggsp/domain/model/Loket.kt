// File: app/src/main/java/com/proyek/maganggsp/domain/model/Loket.kt
package com.proyek.maganggsp.domain.model

/**
 * Merepresentasikan data sebuah loket pembayaran.
 * Ini adalah model inti yang ditampilkan di banyak halaman.
 */
data class Loket(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val address: String,
    val balance: Long,
    val status: String, // Contoh: "Aktif", "Dipantau", "Diblokir"
    val lastAccessed: String? = null

)
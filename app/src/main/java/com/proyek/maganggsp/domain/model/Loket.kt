package com.proyek.maganggsp.domain.model

// Hanya berisi data yang benar-benar dibutuhkan aplikasi
data class Loket(
    val noLoket: String,         // Untuk detail & daftar
    val namaLoket: String,       // Untuk detail & daftar
    val nomorTelepon: String,    // Untuk detail & daftar
    val email: String,           // Hanya untuk detail
    val status: String           // Hanya untuk detail
)
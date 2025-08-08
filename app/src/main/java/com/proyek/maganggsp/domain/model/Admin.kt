// File: app/src/main/java/com/proyek/maganggsp/domain/model/Admin.kt
package com.proyek.maganggsp.domain.model

/**
 * Merepresentasikan data Admin yang sedang login.
 * Digunakan di seluruh aplikasi untuk mengetahui siapa pengguna saat ini.
 */
data class Admin(
    val id: String,
    val name: String,
    val email: String
)
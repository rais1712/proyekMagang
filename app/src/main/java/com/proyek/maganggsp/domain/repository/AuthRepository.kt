// File: app/src/main/java/com/proyek/maganggsp/domain/repository/AuthRepository.kt
package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Admin

/**
 * Kontrak untuk semua aksi yang berhubungan dengan otentikasi.
 */
interface AuthRepository {
    /**
     * Melakukan proses login dan mengembalikan data Admin jika berhasil.
     * Akan melempar Exception jika gagal.
     */
    suspend fun login(email: String, password: String): Admin
}
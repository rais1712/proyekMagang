// File: app/src/main/java/com/proyek/maganggsp/domain/repository/AuthRepository.kt
package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Admin

/**
 * FIXED: Enhanced AuthRepository interface dengan additional methods
 * untuk better session management
 */
interface AuthRepository {
    /**
     * Melakukan proses login dan mengembalikan data Admin jika berhasil.
     * Akan melempar AppException jika gagal.
     */
    suspend fun login(email: String, password: String): Admin

    /**
     * Melakukan logout dan membersihkan session
     */
    suspend fun logout()

    /**
     * Mengecek apakah user sudah login
     */
    fun isLoggedIn(): Boolean
}
// File: app/src/main/java/com/proyek/maganggsp/domain/repository/HistoryRepository.kt
package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Loket

/**
 * Kontrak untuk semua aksi yang berhubungan dengan riwayat.
 */
interface HistoryRepository {
    suspend fun getRecentHistory(): List<Loket>
    suspend fun getFullHistory(): List<Loket>
}
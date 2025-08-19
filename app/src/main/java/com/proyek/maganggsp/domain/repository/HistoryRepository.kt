package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Kontrak untuk semua aksi yang berhubungan dengan riwayat.
 * FIXED: Menggunakan Flow<Resource<T>> pattern yang konsisten dengan repository lain
 */
interface HistoryRepository {
    fun getRecentHistory(): Flow<Resource<List<Loket>>>
    fun getFullHistory(): Flow<Resource<List<Loket>>>
}
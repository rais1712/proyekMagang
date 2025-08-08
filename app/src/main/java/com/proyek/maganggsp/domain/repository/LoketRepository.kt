// File: app/src/main/java/com/proyek/maganggsp/domain/repository/LoketRepository.kt
package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.Mutasi

/**
 * Kontrak untuk semua aksi yang berhubungan dengan data loket dan mutasi.
 */
interface LoketRepository {
    suspend fun getLoketDetails(phoneNumber: String): Loket
    suspend fun getMutations(loketId: String): List<Mutasi>
    suspend fun blockLoket(loketId: String)
    suspend fun unblockLoket(loketId: String)
    suspend fun flagMutation(mutationId: String)
    suspend fun getFlaggedLokets(): List<Loket>
    suspend fun getBlockedLokets(): List<Loket>
    suspend fun searchLoket(query: String): List<Loket>
    suspend fun clearAllFlags(loketId: String)


}
// File: app/src/main/java/com/proyek/maganggsp/domain/repository/LoketRepository.kt - FIXED INTERFACE
package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * FIXED: Complete LoketRepository interface dengan semua required methods untuk MVP
 */
interface LoketRepository {

    /**
     * CORE MVP: Search loket by phone number
     */
    fun searchLoket(phoneNumber: String): Flow<Resource<List<Loket>>>

    /**
     * Get comprehensive loket profile by PPID
     */
    fun getLoketProfile(ppid: String): Flow<Resource<Loket>>

    /**
     * Get transaction logs for specific loket
     */
    fun getLoketTransactions(ppid: String): Flow<Resource<List<TransactionLog>>>

    /**
     * Update loket profile information
     */
    fun updateLoketProfile(ppid: String, updatedLoket: Loket): Flow<Resource<Unit>>

    /**
     * MVP CORE: Block loket functionality
     */
    fun blockLoket(ppid: String): Flow<Resource<Unit>>

    /**
     * MVP CORE: Unblock loket functionality
     */
    fun unblockLoket(ppid: String): Flow<Resource<Unit>>

    /**
     * Access loket by PPID with validation
     */
    fun accessLoketByPpid(ppid: String): Flow<Resource<Loket>>

    /**
     * Get recent loket access history
     */
    fun getRecentLokets(): Flow<Resource<List<Loket>>>

    /**
     * Save loket to access history
     */
    suspend fun saveToHistory(loket: Loket)

    /**
     * Get favorite/bookmarked lokets
     */
    fun getFavoriteLokets(): Flow<Resource<List<Loket>>>

    /**
     * Add/remove loket from favorites
     */
    suspend fun toggleFavorite(ppid: String, isFavorite: Boolean)
}

// File: app/src/main/java/com/proyek/maganggsp/data/repository/LoketRepository.kt - NEW INTERFACE
package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.model.LoketSearchHistory
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * ENHANCED: Loket repository interface for comprehensive loket management
 * Adapts to available API endpoints while providing rich functionality
 */
interface LoketRepository {

    /**
     * Get comprehensive loket profile by PPID
     * Includes profile data and receipts if available
     */
    fun getLoketProfile(ppid: String): Flow<Resource<Loket>>

    /**
     * Get transaction logs for specific loket
     * Maps to /trx/ppid/{ppid} endpoint
     */
    fun getLoketTransactions(ppid: String): Flow<Resource<List<TransactionLog>>>

    /**
     * Update loket profile information
     * Maps to PUT /profiles/ppid/{ppid} endpoint
     */
    fun updateLoketProfile(
        ppid: String,
        updatedLoket: Loket
    ): Flow<Resource<Unit>>

    /**
     * Search/access loket by PPID with validation
     * Since no search endpoint, validate and access directly
     */
    fun accessLoketByPpid(ppid: String): Flow<Resource<Loket>>

    /**
     * Get recent loket access history
     * Local storage of frequently accessed lokets
     */
    fun getRecentLokets(): Flow<Resource<List<LoketSearchHistory>>>

    /**
     * Save loket to access history
     * Track frequently accessed lokets locally
     */
    suspend fun saveToHistory(loket: Loket)

    /**
     * Get favorite/bookmarked lokets
     * Local storage for quick access
     */
    fun getFavoriteLokets(): Flow<Resource<List<Loket>>>

    /**
     * Add/remove loket from favorites
     */
    suspend fun toggleFavorite(ppid: String, isFavorite: Boolean)

    /**
     * FUTURE: When backend supports these endpoints
     */
    // fun searchLoketByPhone(phoneNumber: String): Flow<Resource<List<Loket>>>
    // fun blockLoket(ppid: String): Flow<Resource<Unit>>
    // fun unblockLoket(ppid: String): Flow<Resource<Unit>>
    // fun getBlockedLokets(): Flow<Resource<List<Loket>>>
}
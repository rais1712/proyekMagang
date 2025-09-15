// File: app/src/main/java/com/proyek/maganggsp/domain/repository/LoketRepository.kt - SIMPLIFIED & REAL
package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * SIMPLIFIED: LoketRepository berdasarkan API endpoints yang benar-benar ada
 *
 * REAL API ENDPOINTS:
 * - GET /profiles/ppid/{ppid} -> Profile + receipts
 * - GET /trx/ppid/{ppid} -> Transaction logs
 * - PUT /profiles/ppid/{ppid} -> Update profile (including block/unblock)
 */
interface LoketRepository {

    /**
     * CORE: Get comprehensive loket profile with receipts
     * Maps to: GET /profiles/ppid/{ppid}
     */
    fun getLoketProfile(ppid: String): Flow<Resource<Loket>>

    /**
     * CORE: Get transaction logs for specific loket
     * Maps to: GET /trx/ppid/{ppid}
     */
    fun getLoketTransactions(ppid: String): Flow<Resource<List<TransactionLog>>>

    /**
     * CORE: Block loket functionality
     * Implementation: PUT /profiles/ppid/{ppid} with body {"mpPpid": "ppid + blok"}
     */
    fun blockLoket(ppid: String): Flow<Resource<Unit>>

    /**
     * CORE: Unblock loket functionality
     * Implementation: PUT /profiles/ppid/{ppid} with body {"mpPpid": "ppid without blok"}
     */
    fun unblockLoket(ppid: String): Flow<Resource<Unit>>

    /**
     * CORE: Update loket profile information
     * Maps to: PUT /profiles/ppid/{ppid}
     */
    fun updateLoketProfile(ppid: String, updatedLoket: Loket): Flow<Resource<Unit>>

    /**
     * CONVENIENCE: Access loket by PPID (same as getLoketProfile)
     */
    fun accessLoketByPpid(ppid: String): Flow<Resource<Loket>>

    /**
     * LOCAL: Search functionality (no API endpoint, uses local history)
     * Since API doesn't have search endpoint, implement using local storage
     */
    fun searchLoket(phoneNumber: String): Flow<Resource<List<Loket>>>

    /**
     * LOCAL: Recent lokets from access history
     */
    fun getRecentLokets(): Flow<Resource<List<Loket>>>

    /**
     * LOCAL: Save loket to access history
     */
    suspend fun saveToHistory(loket: Loket)

    /**
     * LOCAL: Favorite lokets management
     */
    fun getFavoriteLokets(): Flow<Resource<List<Loket>>>
    suspend fun toggleFavorite(ppid: String, isFavorite: Boolean)
}

/**
 * USAGE NOTES:
 *
 * 1. Block/Unblock Logic:
 *    - Block: Change ppid from "PIDLKTD0025" to "PIDLKTD0025blok"
 *    - Unblock: Change ppid from "PIDLKTD0025blok" to "PIDLKTD0025"
 *    - Status determined by checking if ppid ends with "blok"
 *
 * 2. Search Limitation:
 *    - No API endpoint for search by phone number
 *    - searchLoket() searches through local history only
 *    - For direct access, use accessLoketByPpid() with known PPID
 *
 * 3. Data Flow:
 *    - getLoketProfile() → Auto saves to history
 *    - blockLoket()/unblockLoket() → Updates profile via API
 *    - Recent/Favorites → Local storage only
 */
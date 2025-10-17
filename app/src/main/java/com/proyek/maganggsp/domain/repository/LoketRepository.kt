// File: app/src/main/java/com/proyek/maganggsp/domain/repository/LoketRepository.kt
package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface untuk Loket operations
 */
interface LoketRepository {

    /**
     * Search profiles by PPID
     */
    suspend fun searchProfiles(ppid: String): Flow<Resource<List<Receipt>>>

    /**
     * Get recent accessed profiles
     */
    suspend fun getRecentProfiles(): Flow<Resource<List<Receipt>>>

    /**
     * Save search history
     */
    suspend fun saveSearchHistory(ppid: String)

    /**
     * Get search history
     */
    suspend fun getSearchHistory(): Flow<List<String>>

    /**
     * Clear search history
     */
    suspend fun clearSearchHistory()
}

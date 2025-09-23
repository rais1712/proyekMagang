// =================================================================
// File: app/src/main/java/com/proyek/maganggsp/domain/repository/LoketRepository.kt
package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.LoketSearchHistory
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * MODULAR: Loket Repository Interface (KEPT EXISTING)
 * For local history management and loket operations
 */
interface LoketRepository {

    /**
     * Get loket profile with comprehensive data
     */
    fun getLoketProfile(ppid: String): Flow<Resource<Loket>>

    /**
     * Block/unblock operations
     */
    fun blockLoket(ppid: String): Flow<Resource<Unit>>
    fun unblockLoket(ppid: String): Flow<Resource<Unit>>

    /**
     * Local history management
     */
    fun getRecentLokets(): Flow<Resource<List<Loket>>>
    fun searchLoket(ppidQuery: String): Flow<Resource<List<Loket>>>
    suspend fun saveToHistory(loket: Loket)

    /**
     * Favorites management
     */
    fun getFavoriteLokets(): Flow<Resource<List<Loket>>>
    suspend fun toggleFavorite

// File: app/src/main/java/com/proyek/maganggsp/domain/repository/ProfileRepository.kt - UNIFIED
package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * UNIFIED REPOSITORY: Single source for all profile operations
 * Replaces LoketRepository with Receipt/TransactionLog focus
 */
interface ProfileRepository {

    /**
     * PRIMARY: Get profile data (maps to Receipt for home screen)
     */
    fun getProfile(ppid: String): Flow<Resource<Receipt>>

    /**
     * PRIMARY: Get transaction logs (for detail screen)
     */
    fun getTransactionLogs(ppid: String): Flow<Resource<List<TransactionLog>>>

    /**
     * PRIMARY: Update profile (block/unblock operations)
     */
    fun updateProfile(currentPpid: String, newPpid: String): Flow<Resource<Unit>>

    /**
     * SEARCH: Find profiles by PPID pattern
     */
    fun searchProfiles(ppidQuery: String): Flow<Resource<List<Receipt>>>
}
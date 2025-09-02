// File: app/src/main/java/com/proyek/maganggsp/domain/repository/ProfileRepository.kt
package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * NEW REPOSITORY: ProfileRepository - replaces LoketRepository
 * Handles new API endpoints for receipt and transaction data
 *
 * TARGET ENDPOINTS:
 * - GET /profiles/ppid/{ppid} -> Receipt data
 * - GET /trx/ppid/{ppid} -> Transaction logs
 * - PUT /profiles/ppid/{ppid} -> Update profile
 */
interface ProfileRepository {

    /**
     * Get profile/receipt data for a specific ppid
     * Maps to Receipt domain model for home screen display
     */
    fun getProfile(ppid: String): Flow<Resource<Receipt>>

    /**
     * Get transaction logs for a specific ppid
     * Maps to TransactionLog domain model for detail screen
     */
    fun getTransactionLogs(ppid: String): Flow<Resource<List<TransactionLog>>>

    /**
     * Update profile information
     * Used for profile management operations
     */
    fun updateProfile(ppid: String, newPpid: String): Flow<Resource<Unit>>

    /**
     * Search profiles/receipts by query
     * Replaces loket search functionality
     */
    fun searchProfiles(query: String): Flow<Resource<List<Receipt>>>
}
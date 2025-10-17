// File: app/src/main/java/com/proyek/maganggsp/domain/repository/TransactionRepository.kt
package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Repository interface untuk Transaction operations
 */
interface TransactionRepository {

    /**
     * Get transaction logs by PPID
     */
    suspend fun getTransactionLogs(ppid: String): Flow<Resource<List<TransactionLog>>>

    /**
     * Get transaction logs dengan filter
     */
    suspend fun getTransactionLogsWithFilter(
        ppid: String,
        startDate: String? = null,
        endDate: String? = null
    ): Flow<Resource<List<TransactionLog>>>
}

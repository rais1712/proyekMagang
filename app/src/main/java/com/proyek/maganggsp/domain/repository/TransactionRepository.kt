// =================================================================
// File: app/src/main/java/com/proyek/maganggsp/domain/repository/TransactionRepository.kt
package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow

/**
 * MODULAR: Transaction Repository Interface
 * Handles transaction log operations for detail screens
 */
interface TransactionRepository {

    /**
     * Get transaction logs for specific PPID
     * Source: GET /trx/ppid/{ppid}
     */
    fun getTransactionLogs(ppid: String): Flow<Resource<List<TransactionLog>>>

    /**
     * Get transaction summary statistics
     */
    fun getTransactionSummary(ppid: String): Flow<Resource<TransactionSummary>>
}

/**
 * Transaction summary data class
 */
data class TransactionSummary(
    val totalCount: Int,
    val incomingCount: Int,
    val outgoingCount: Int,
    val totalIncoming: Long,
    val totalOutgoing: Long,
    val netAmount: Long,
    val latestBalance: Long
)
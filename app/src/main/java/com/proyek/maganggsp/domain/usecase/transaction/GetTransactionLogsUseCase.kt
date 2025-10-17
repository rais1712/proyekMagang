// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/transaction/GetTransactionLogsUseCase.kt
package com.proyek.maganggsp.domain.usecase.transaction

import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.repository.TransactionRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case untuk get transaction logs by PPID
 */
class GetTransactionLogsUseCase @Inject constructor(
    private val repository: TransactionRepository
) {

    /**
     * Get transaction logs untuk specific PPID
     */
    suspend fun execute(ppid: String): Flow<Resource<List<TransactionLog>>> {
        return repository.getTransactionLogs(ppid)
    }

    /**
     * Get transaction logs dengan filter tanggal
     */
    suspend fun executeWithFilter(
        ppid: String,
        startDate: String? = null,
        endDate: String? = null
    ): Flow<Resource<List<TransactionLog>>> {
        return repository.getTransactionLogsWithFilter(ppid, startDate, endDate)
    }
}

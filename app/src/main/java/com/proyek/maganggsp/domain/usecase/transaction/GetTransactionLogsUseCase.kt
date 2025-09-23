// =================================================================
// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/transaction/GetTransactionLogsUseCase.kt
package com.proyek.maganggsp.domain.usecase.transaction

import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.repository.TransactionRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * MODULAR: Get transaction logs use case for detail screens
 */
class GetTransactionLogsUseCase @Inject constructor(
    private val transactionRepository: TransactionRepository
) {
    operator fun invoke(ppid: String): Flow<Resource<List<TransactionLog>>> {
        return transactionRepository.getTransactionLogs(ppid)
    }
}
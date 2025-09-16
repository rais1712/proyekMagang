// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/GetLoketTransactionsUseCase.kt
package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetLoketTransactionsUseCase @Inject constructor(
    private val loketRepository: LoketRepository
) {
    operator fun invoke(ppid: String): Flow<Resource<List<TransactionLog>>> {
        return loketRepository.getLoketTransactions(ppid)
    }
}
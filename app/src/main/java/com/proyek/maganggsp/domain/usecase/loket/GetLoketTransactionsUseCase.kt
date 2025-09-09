// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/GetLoketTransactionsUseCase.kt
package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.repository.LoketRepository
import javax.inject.Inject

class GetLoketTransactionsUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    operator fun invoke(ppid: String) = repository.getLoketTransactions(ppid)
}
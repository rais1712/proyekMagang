// GetFullHistoryUseCase.kt
package com.proyek.maganggsp.domain.usecase.history

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.HistoryRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetFullHistoryUseCase @Inject constructor(
    private val repository: HistoryRepository
) {
    // FIXED: Langsung return dari repository karena sudah Flow<Resource<T>>
    operator fun invoke(): Flow<Resource<List<Loket>>> {
        return repository.getFullHistory()
    }
}
package com.proyek.maganggsp.domain.usecase.history

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.NetworkResult
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentHistoryUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    operator fun invoke(): Flow<NetworkResult<List<Loket>>> {
        return repository.getRecentHistory()
    }
}

class GetFullHistoryUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    operator fun invoke(): Flow<NetworkResult<List<Loket>>> {
        return repository.getFullHistory()
    }
}

package com.proyek.maganggsp.domain.usecase.mutasi

import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.NetworkResult
import javax.inject.Inject

class FlagTransactionUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    suspend operator fun invoke(mutationId: String): NetworkResult<Unit> {
        if (mutationId.isBlank()) {
            return NetworkResult.Error(message = "ID mutasi tidak boleh kosong")
        }
        return repository.flagTransaction(mutationId)
    }
}

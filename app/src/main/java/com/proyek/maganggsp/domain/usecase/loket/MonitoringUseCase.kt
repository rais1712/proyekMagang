package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.NetworkResult
import javax.inject.Inject

class GetFlaggedLoketsUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    suspend operator fun invoke(): NetworkResult<List<Loket>> {
        return repository.getFlaggedLokets()
    }
}

class GetBlockedLoketsUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    suspend operator fun invoke(): NetworkResult<List<Loket>> {
        return repository.getBlockedLokets()
    }
}

class BlockLoketUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    suspend operator fun invoke(loketNumber: String): NetworkResult<Unit> {
        if (loketNumber.isBlank()) {
            return NetworkResult.Error(message = "Nomor loket tidak boleh kosong")
        }
        return repository.blockLoket(loketNumber)
    }
}

class UnblockLoketUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    suspend operator fun invoke(loketNumber: String): NetworkResult<Unit> {
        if (loketNumber.isBlank()) {
            return NetworkResult.Error(message = "Nomor loket tidak boleh kosong")
        }
        return repository.unblockLoket(loketNumber)
    }
}

package com.proyek.maganggsp.domain.usecase.mutasi

import com.proyek.maganggsp.domain.model.Mutasi
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.NetworkResult
import javax.inject.Inject

class GetMutasiLoketUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    suspend operator fun invoke(loketNumber: String): NetworkResult<List<Mutasi>> {
        if (loketNumber.isBlank()) {
            return NetworkResult.Error(message = "Nomor loket tidak boleh kosong")
        }
        return repository.getMutations(loketNumber)
    }
}

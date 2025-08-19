package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetBlockedLoketsUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    // FIXED: Langsung return dari repository karena sudah Flow<Resource<List<Loket>>>
    // Hapus manual flow builder dan exception handling yang duplikat
    operator fun invoke(): Flow<Resource<List<Loket>>> {
        return repository.getBlockedLokets()
    }
}
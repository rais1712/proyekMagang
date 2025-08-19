package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

class SearchLoketUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    operator fun invoke(query: String): Flow<Resource<List<Loket>>> {
        // FIXED: Cek query kosong dulu, baru call repository yang sudah return Flow<Resource<T>>
        return if (query.isBlank()) {
            // Jika query kosong, return empty success
            flowOf(Resource.Success(emptyList()))
        } else {
            // FIXED: Langsung return dari repository tanpa manual flow wrapper
            repository.searchLoket(query)
        }
    }
}
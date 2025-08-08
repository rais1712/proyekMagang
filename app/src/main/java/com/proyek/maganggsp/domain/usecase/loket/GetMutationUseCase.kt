// Lokasi: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/GetMutationsUseCase.kt
package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.model.Mutasi
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetMutationUseCase @Inject constructor(private val repository: LoketRepository) {
    operator fun invoke(loketId: String): Flow<Resource<List<Mutasi>>> = flow {
        try {
            emit(Resource.Loading())
            val mutations = repository.getMutations(loketId)
            emit(Resource.Success(mutations))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal memuat daftar mutasi"))
        }
    }
}
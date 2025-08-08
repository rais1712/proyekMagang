package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class FlagMutationUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    operator fun invoke(mutationId: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            // Memanggil fungsi di repository untuk menandai mutasi
            repository.flagMutation(mutationId)
            // Emit success
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal menandai mutasi."))
        }
    }
}
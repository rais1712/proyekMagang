package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class BlockLoketUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    operator fun invoke(loketId: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            // Memanggil fungsi di repository untuk memblokir loket
            repository.blockLoket(loketId)
            // Emit success dengan data Unit (karena tidak ada data yang dikembalikan)
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal memblokir loket."))
        }
    }
}
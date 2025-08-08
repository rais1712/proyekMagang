package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class UnblockLoketUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    operator fun invoke(loketId: String): Flow<Resource<Unit>> = flow {
        try {
            emit(Resource.Loading())
            // Memanggil fungsi di repository untuk membuka blokir loket
            repository.unblockLoket(loketId)
            // Emit success
            emit(Resource.Success(Unit))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal membuka blokir loket."))
        }
    }
}
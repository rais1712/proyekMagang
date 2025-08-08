// Lokasi: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/GetLoketDetailsUseCase.kt
package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class GetLoketDetailUseCase @Inject constructor(private val repository: LoketRepository) {
    operator fun invoke(phoneNumber: String): Flow<Resource<Loket>> = flow {
        try {
            emit(Resource.Loading())
            val loket = repository.getLoketDetails(phoneNumber)
            emit(Resource.Success(loket))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Gagal memuat detail loket"))
        }
    }
}
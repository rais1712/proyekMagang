// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/GetRecentLoketsUseCase.kt
package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class GetRecentLoketsUseCase @Inject constructor(
    private val loketRepository: LoketRepository
) {
    operator fun invoke(): Flow<Resource<List<Loket>>> {
        return loketRepository.getRecentLokets()
    }
}
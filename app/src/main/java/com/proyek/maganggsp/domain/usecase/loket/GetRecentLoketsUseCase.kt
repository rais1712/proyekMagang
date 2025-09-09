// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/GetRecentLoketsUseCase.kt
package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.repository.LoketRepository
import javax.inject.Inject

class GetRecentLoketsUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    operator fun invoke() = repository.getRecentLokets()
}
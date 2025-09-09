// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/GetLoketProfileUseCase.kt
package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.repository.LoketRepository
import javax.inject.Inject

class GetLoketProfileUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    operator fun invoke(ppid: String) = repository.getLoketProfile(ppid)
}
// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/UpdateLoketProfileUseCase.kt
package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.LoketRepository
import javax.inject.Inject

class UpdateLoketProfileUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    operator fun invoke(ppid: String, updatedLoket: Loket) =
        repository.updateLoketProfile(ppid, updatedLoket)
}
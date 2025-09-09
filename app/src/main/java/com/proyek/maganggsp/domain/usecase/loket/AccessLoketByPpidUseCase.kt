// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/AccessLoketByPpidUseCase.kt
package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.domain.model.Loket
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for accessing loket by PPID with validation and history tracking
 */
class AccessLoketByPpidUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    operator fun invoke(ppid: String): Flow<Resource<Loket>> {
        return repository.accessLoketByPpid(ppid)
    }
}

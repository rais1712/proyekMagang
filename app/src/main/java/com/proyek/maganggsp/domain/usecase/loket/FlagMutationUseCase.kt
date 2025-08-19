package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class FlagMutationUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    // FIXED: Menggunakan pattern yang konsisten dengan use case lain
    // Langsung return flow dari repository tanpa wrapper tambahan
    operator fun invoke(mutationId: String): Flow<Resource<Unit>> {
        return repository.flagMutation(mutationId)
    }
}
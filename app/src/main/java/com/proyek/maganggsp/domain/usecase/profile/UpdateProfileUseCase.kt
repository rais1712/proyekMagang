// =================================================================
// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/profile/UpdateProfileUseCase.kt
package com.proyek.maganggsp.domain.usecase.profile

import com.proyek.maganggsp.domain.repository.ProfileRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * MODULAR: Update profile use case for block/unblock operations
 */
class UpdateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(currentPpid: String, newPpid: String): Flow<Resource<Unit>> {
        return profileRepository.updateProfile(currentPpid, newPpid)
    }
}
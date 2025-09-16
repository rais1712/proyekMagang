// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/UpdateProfileUseCase.kt
package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.repository.ProfileRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(currentPpid: String, newPpid: String): Flow<Resource<Unit>> {
        return profileRepository.updateProfile(currentPpid, newPpid)
    }
}
// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/profile/UpdateProfileUseCase.kt
package com.proyek.maganggsp.domain.usecase.profile

import com.proyek.maganggsp.domain.repository.ProfileRepository
import javax.inject.Inject

class UpdateProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(ppid: String, newPpid: String) = repository.updateProfile(ppid, newPpid)
}
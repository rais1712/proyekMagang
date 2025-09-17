// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/profile/GetProfileUseCase.kt
package com.proyek.maganggsp.domain.usecase.loketprofile

import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.repository.ProfileRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * UNIFIED: Get profile data (maps to Receipt for home screen)
 */
class GetProfileUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(ppid: String): Flow<Resource<Receipt>> {
        return profileRepository.getProfile(ppid)
    }
}







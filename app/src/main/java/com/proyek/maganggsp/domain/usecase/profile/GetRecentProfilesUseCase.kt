// =================================================================
// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/profile/GetRecentProfilesUseCase.kt
package com.proyek.maganggsp.domain.usecase.profile

import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.repository.ProfileRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * MODULAR: Get recent profiles use case for home screen
 */
class GetRecentProfilesUseCase @Inject constructor(
    private val profileRepository: ProfileRepository
) {
    operator fun invoke(): Flow<Resource<List<Receipt>>> {
        return profileRepository.getRecentProfiles()
    }
}

// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/profile/SearchProfilesUseCase.kt
package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.repository.ProfileRepository
import javax.inject.Inject

class SearchProfilesUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(query: String) = repository.searchProfiles(query)
}
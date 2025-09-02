package com.proyek.maganggsp.domain.usecase.profile

import com.proyek.maganggsp.domain.repository.ProfileRepository
import javax.inject.Inject

class GetProfileUseCase @Inject constructor(
    private val repository: ProfileRepository
) {
    operator fun invoke(ppid: String) = repository.getProfile(ppid)
}
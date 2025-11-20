// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/auth/LogoutUseCase.kt

package com.proyek.maganggsp.domain.usecase.auth

import com.proyek.maganggsp.domain.repository.AuthRepository
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke() {
        repository.logout()
    }
}

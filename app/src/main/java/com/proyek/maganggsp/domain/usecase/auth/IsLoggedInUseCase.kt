// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/auth/IsLoggedInUseCase.kt

package com.proyek.maganggsp.domain.usecase.auth

import com.proyek.maganggsp.domain.repository.AuthRepository
import javax.inject.Inject

class IsLoggedInUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(): Boolean {
        return repository.isLoggedIn()
    }
}

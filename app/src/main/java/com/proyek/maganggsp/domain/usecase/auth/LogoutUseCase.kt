package com.proyek.maganggsp.domain.usecase.auth

import com.proyek.maganggsp.domain.repository.AuthRepository
import com.proyek.maganggsp.util.NetworkResult
import javax.inject.Inject

class LogoutUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(): NetworkResult<Unit> {
        val result = repository.logout()
        if (result is NetworkResult.Success) {
            repository.clearStoredToken()
        }
        return result
    }
}

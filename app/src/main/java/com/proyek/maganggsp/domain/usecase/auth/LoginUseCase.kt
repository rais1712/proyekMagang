package com.proyek.maganggsp.domain.usecase.auth

import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.repository.AuthRepository
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): Admin {
        return repository.login(email, password)
    }
}

package com.proyek.maganggsp.domain.usecase.auth

import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.repository.AuthRepository
import com.proyek.maganggsp.util.NetworkResult
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    suspend operator fun invoke(email: String, password: String): NetworkResult<Admin> {
        if (email.isBlank()) {
            return NetworkResult.Error(message = "Email tidak boleh kosong")
        }
        if (password.isBlank()) {
            return NetworkResult.Error(message = "Password tidak boleh kosong")
        }
        if (!email.contains("@")) {
            return NetworkResult.Error(message = "Format email tidak valid")
        }

        return repository.login(email, password)
    }
}

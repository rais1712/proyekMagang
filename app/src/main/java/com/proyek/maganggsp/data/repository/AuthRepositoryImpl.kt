package com.proyek.maganggsp.data.repository

import com.proyek.maganggsp.data.remote.api.AuthApi
import com.proyek.maganggsp.data.remote.api.LoginRequest
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.repository.AuthRepository
import com.proyek.maganggsp.util.Resource
import javax.inject.Inject

class AuthRepositoryImpl @Inject constructor(
    private val api: AuthApi
) : AuthRepository {

    override suspend fun login(email: String, password: String): Resource<Admin> {
        return try {
            val response = api.login(LoginRequest(email, password))
            if (response.isSuccessful) {
                val adminDto = response.body()?.admin
                if (adminDto != null) {
                    Resource.Success(
                        Admin(
                            id = adminDto.id,
                            email = adminDto.email,
                            name = adminDto.name,
                            role = adminDto.role
                        )
                    )
                } else {
                    Resource.Error("Response body is null")
                }
            } else {
                Resource.Error("Login failed: ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error(e.message ?: "An error occurred")
        }
    }
}

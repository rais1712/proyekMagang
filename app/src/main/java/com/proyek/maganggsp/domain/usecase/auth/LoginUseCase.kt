package com.proyek.maganggsp.domain.usecase.auth

import com.google.gson.Gson
// FIX: Updated import path to match corrected ErrorResponseDto location
import com.proyek.maganggsp.data.dto.ErrorResponseDto
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.repository.AuthRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import retrofit2.HttpException
import java.io.IOException
import javax.inject.Inject

class LoginUseCase @Inject constructor(
    private val repository: AuthRepository
) {
    operator fun invoke(email: String, password: String): Flow<Resource<Admin>> = flow {
        try {
            emit(Resource.Loading())
            val admin = repository.login(email, password)
            emit(Resource.Success(admin))

        } catch (e: HttpException) {
            // Enhanced error parsing with corrected import
            val errorMessage = try {
                val errorJson = e.response()?.errorBody()?.string()
                Gson().fromJson(errorJson, ErrorResponseDto::class.java).message
                    ?: "Terjadi kesalahan yang tidak terduga."
            } catch (jsonError: Exception) {
                // Fallback to HTTP exception message
                e.localizedMessage ?: "Terjadi kesalahan yang tidak terduga."
            }
            emit(Resource.Error(errorMessage))

        } catch (e: IOException) {
            emit(Resource.Error("Tidak dapat terhubung ke server. Periksa koneksi internet Anda."))
        } catch (e: Exception) {
            emit(Resource.Error(e.localizedMessage ?: "Terjadi kesalahan."))
        }
    }
}
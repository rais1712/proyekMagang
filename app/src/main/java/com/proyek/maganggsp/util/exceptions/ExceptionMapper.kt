// File: app/src/main/java/com/proyek/maganggsp/util/exceptions/ExceptionMapper.kt
package com.proyek.maganggsp.util.exceptions

import com.google.gson.Gson
import com.proyek.maganggsp.data.dto.ErrorResponseDto
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionMapper @Inject constructor() {

    /**
     * Maps berbagai jenis exception ke AppException yang lebih user-friendly
     */
    fun mapToAppException(throwable: Throwable): AppException {
        return when (throwable) {
            // Network related exceptions
            is UnknownHostException -> AppException.NetworkException(
                "Server tidak dapat dijangkau. Periksa koneksi internet Anda."
            )
            is ConnectException -> AppException.NetworkException(
                "Gagal terhubung ke server. Pastikan Anda terhubung ke internet."
            )
            is SocketTimeoutException -> AppException.NetworkException(
                "Koneksi timeout. Silakan coba lagi."
            )
            is IOException -> AppException.NetworkException(
                "Masalah jaringan. Periksa koneksi internet Anda."
            )

            // HTTP exceptions
            is HttpException -> mapHttpException(throwable)

            // App specific exceptions
            is AppException -> throwable

            // Unknown exceptions
            else -> AppException.UnknownException(
                throwable.localizedMessage ?: "Terjadi kesalahan yang tidak terduga.",
                throwable
            )
        }
    }

    /**
     * Maps HTTP exceptions berdasarkan status code dan response body
     */
    private fun mapHttpException(httpException: HttpException): AppException {
        val code = httpException.code()
        val errorMessage = parseErrorMessage(httpException)

        return when (code) {
            400 -> AppException.ValidationException(
                errorMessage ?: "Data yang dikirim tidak valid."
            )
            401 -> AppException.AuthenticationException(
                errorMessage ?: "Sesi Anda telah berakhir. Silakan login kembali."
            )
            403 -> AppException.UnauthorizedException(
                errorMessage ?: "Anda tidak memiliki izin untuk melakukan aksi ini."
            )
            404 -> AppException.ServerException(
                code,
                errorMessage ?: "Data yang diminta tidak ditemukan."
            )
            422 -> AppException.ValidationException(
                errorMessage ?: "Data yang dikirim tidak sesuai format."
            )
            in 500..599 -> AppException.ServerException(
                code,
                errorMessage ?: "Server sedang mengalami masalah. Silakan coba lagi nanti."
            )
            else -> AppException.ServerException(
                code,
                errorMessage ?: "Terjadi kesalahan pada server (HTTP $code)."
            )
        }
    }

    /**
     * Parse error message dari HTTP response body
     */
    private fun parseErrorMessage(httpException: HttpException): String? {
        return try {
            val errorBody = httpException.response()?.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                // Simple JSON parsing tanpa Gson untuk menghindari dependency issues
                // Look for "message" field in JSON response
                val messageRegex = "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                val matchResult = messageRegex.find(errorBody)
                matchResult?.groups?.get(1)?.value
            } else null
        } catch (e: Exception) {
            // Jika gagal parsing, return null agar menggunakan default message
            null
        }
    }
}
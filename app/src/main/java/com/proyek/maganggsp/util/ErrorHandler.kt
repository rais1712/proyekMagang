// File: app/src/main/java/com/proyek/maganggsp/util/ErrorHandler.kt
package com.proyek.maganggsp.util

import android.content.Context
import android.widget.Toast
import com.proyek.maganggsp.util.exceptions.AppException

/**
 * MODULAR: Error handling utilities
 * Extracted from unified AppUtils.kt for better modularity
 */
object ErrorHandler {

    /**
     * Show error message with appropriate user-friendly text
     */
    fun showError(context: Context, exception: AppException) {
        val message = getUserFriendlyMessage(exception)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Show error message with custom message
     */
    fun showError(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Show success message
     */
    fun showSuccess(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Convert AppException to user-friendly message
     */
    private fun getUserFriendlyMessage(exception: AppException): String {
        return when (exception) {
            is AppException.NetworkException -> {
                "Tidak dapat terhubung ke server. Periksa koneksi internet Anda."
            }
            is AppException.AuthenticationException -> {
                "Sesi Anda telah berakhir. Silakan login kembali."
            }
            is AppException.ServerException -> {
                when (exception.httpCode) {
                    400 -> "Data yang dikirim tidak valid."
                    401 -> "Akses tidak diizinkan. Silakan login kembali."
                    403 -> "Anda tidak memiliki izin untuk melakukan aksi ini."
                    404 -> "Data yang diminta tidak ditemukan."
                    422 -> "Data yang dikirim tidak sesuai format."
                    429 -> "Terlalu banyak permintaan. Coba lagi nanti."
                    in 500..599 -> "Server sedang bermasalah. Coba lagi nanti."
                    else -> "Terjadi kesalahan server (${exception.httpCode})."
                }
            }
            is AppException.ValidationException -> {
                exception.message
            }
            is AppException.ParseException -> {
                "Terjadi kesalahan dalam memproses data."
            }
            is AppException.UnauthorizedException -> {
                "Anda tidak memiliki izin untuk melakukan aksi ini."
            }
            is AppException.UnknownException -> {
                "Terjadi kesalahan yang tidak terduga."
            }
        }
    }

    /**
     * Check if error is retryable
     */
    fun isRetryableError(exception: AppException): Boolean {
        return when (exception) {
            is AppException.NetworkException -> true
            is AppException.ServerException -> exception.httpCode in 500..599
            else -> false
        }
    }

    /**
     * Get error title for dialogs
     */
    fun getErrorTitle(exception: AppException): String {
        return when (exception) {
            is AppException.NetworkException -> "Masalah Jaringan"
            is AppException.AuthenticationException -> "Masalah Autentikasi"
            is AppException.ServerException -> "Masalah Server"
            is AppException.ValidationException -> "Data Tidak Valid"
            else -> "Terjadi Kesalahan"
        }
    }
}
// File: app/src/main/java/com/proyek/maganggsp/util/exceptions/AppExceptions.kt
package com.proyek.maganggsp.util.exceptions

/**
 * Base exception class untuk semua custom exceptions di aplikasi
 */
sealed class AppException(
    override val message: String,
    override val cause: Throwable? = null
) : Exception(message, cause) {

    /**
     * Exception untuk masalah jaringan/koneksi
     */
    class NetworkException(
        message: String = "Gagal terhubung ke server. Periksa koneksi internet Anda.",
        cause: Throwable? = null
    ) : AppException(message, cause)

    /**
     * Exception untuk masalah autentikasi
     */
    class AuthenticationException(
        message: String = "Sesi Anda telah berakhir. Silakan login kembali.",
        cause: Throwable? = null
    ) : AppException(message, cause)

    /**
     * Exception untuk response server error (4xx, 5xx)
     */
    class ServerException(
        val httpCode: Int,
        message: String,
        cause: Throwable? = null
    ) : AppException(message, cause)

    /**
     * Exception untuk parsing/serialization error
     */
    class ParseException(
        message: String = "Terjadi kesalahan dalam memproses data.",
        cause: Throwable? = null
    ) : AppException(message, cause)

    /**
     * Exception untuk validasi input
     */
    class ValidationException(
        message: String,
        cause: Throwable? = null
    ) : AppException(message, cause)

    /**
     * Exception untuk aksi yang tidak diizinkan
     */
    class UnauthorizedException(
        message: String = "Anda tidak memiliki izin untuk melakukan aksi ini.",
        cause: Throwable? = null
    ) : AppException(message, cause)

    /**
     * Exception umum untuk error yang tidak terduga
     */
    class UnknownException(
        message: String = "Terjadi kesalahan yang tidak terduga.",
        cause: Throwable? = null
    ) : AppException(message, cause)
}
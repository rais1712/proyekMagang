// File: app/src/main/java/com/proyek/maganggsp/util/exceptions/ExceptionMapper.kt
package com.proyek.maganggsp.util.exceptions

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.proyek.maganggsp.BuildConfig
import retrofit2.HttpException
import java.io.IOException
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ExceptionMapper @Inject constructor() {

    companion object {
        private const val DEVELOPMENT_SERVER_IP = "192.168.168.6"
        private const val DEVELOPMENT_SERVER_PORT = 8180
        private const val DEVELOPMENT_SERVER_URL = "http://$DEVELOPMENT_SERVER_IP:$DEVELOPMENT_SERVER_PORT"
    }

    // Context for network checking - will be injected when needed
    private var context: Context? = null

    fun setContext(context: Context) {
        this.context = context
    }

    /**
     * ENHANCED: Maps berbagai jenis exception dengan network awareness
     */
    fun mapToAppException(throwable: Throwable): AppException {
        val hasNetwork = isNetworkAvailable()
        val isDevEnvironment = BuildConfig.DEBUG

        return when (throwable) {
            // Network specific exceptions with context
            is UnknownHostException -> mapUnknownHostException(hasNetwork, isDevEnvironment)
            is ConnectException -> mapConnectException(hasNetwork, isDevEnvironment)
            is SocketTimeoutException -> mapTimeoutException(hasNetwork)
            is IOException -> mapIOException(hasNetwork)

            // HTTP exceptions with enhanced context
            is HttpException -> mapHttpExceptionEnhanced(throwable, hasNetwork)

            // App specific exceptions pass through
            is AppException -> throwable

            // Unknown exceptions with context
            else -> mapUnknownException(throwable, hasNetwork)
        }
    }

    /**
     * NETWORK AVAILABILITY CHECK
     */
    private fun isNetworkAvailable(): Boolean {
        val context = this.context ?: return false

        return try {
            val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val network = connectivityManager.activeNetwork ?: return false
            val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false

            capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                    capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
        } catch (e: Exception) {
            false
        }
    }

    /**
     * ENHANCED EXCEPTION MAPPING with contextual messages
     */

    private fun mapUnknownHostException(hasNetwork: Boolean, isDevEnvironment: Boolean): AppException.NetworkException {
        val message = when {
            !hasNetwork -> "Tidak ada koneksi internet. Periksa WiFi atau data seluler Anda."
            isDevEnvironment -> """
                Server development tidak dapat dijangkau.
                
                Pastikan:
                • Server berjalan di $DEVELOPMENT_SERVER_URL
                • Anda terhubung ke jaringan yang sama dengan server
                • Firewall tidak memblokir port $DEVELOPMENT_SERVER_PORT
                """.trimIndent()
            else -> "Server tidak dapat dijangkau. Periksa koneksi internet Anda."
        }

        return AppException.NetworkException(message)
    }

    private fun mapConnectException(hasNetwork: Boolean, isDevEnvironment: Boolean): AppException.NetworkException {
        val message = when {
            !hasNetwork -> "Koneksi internet tidak stabil. Coba lagi setelah koneksi membaik."
            isDevEnvironment -> """
                Tidak dapat terhubung ke server development.
                
                Solusi:
                • Pastikan server backend berjalan
                • Cek apakah API tersedia di $DEVELOPMENT_SERVER_URL/api
                • Restart server jika perlu
                """.trimIndent()
            else -> "Gagal terhubung ke server. Periksa koneksi internet dan coba lagi."
        }

        return AppException.NetworkException(message)
    }

    private fun mapTimeoutException(hasNetwork: Boolean): AppException.NetworkException {
        val message = when {
            !hasNetwork -> "Koneksi internet terlalu lambat. Periksa kecepatan internet Anda."
            else -> """
                Koneksi timeout. Server mungkin sedang lambat atau overload.
                
                Coba:
                • Tunggu beberapa saat dan coba lagi
                • Periksa kecepatan internet Anda
                """.trimIndent()
        }

        return AppException.NetworkException(message)
    }

    private fun mapIOException(hasNetwork: Boolean): AppException.NetworkException {
        val message = when {
            !hasNetwork -> "Masalah koneksi internet. Periksa jaringan Anda."
            else -> "Terjadi masalah jaringan. Coba lagi dalam beberapa saat."
        }

        return AppException.NetworkException(message)
    }

    /**
     * ENHANCED HTTP EXCEPTION MAPPING
     */
    private fun mapHttpExceptionEnhanced(httpException: HttpException, hasNetwork: Boolean): AppException {
        val code = httpException.code()
        val errorMessage = parseErrorMessage(httpException)

        return when (code) {
            400 -> AppException.ValidationException(
                errorMessage ?: getContextualValidationMessage()
            )
            401 -> AppException.AuthenticationException(
                errorMessage ?: getContextualAuthMessage(hasNetwork)
            )
            403 -> AppException.UnauthorizedException(
                errorMessage ?: "Anda tidak memiliki izin untuk mengakses resource ini."
            )
            404 -> AppException.ServerException(
                code,
                errorMessage ?: getContextual404Message()
            )
            422 -> AppException.ValidationException(
                errorMessage ?: "Data yang dikirim tidak sesuai format yang diharapkan server."
            )
            429 -> AppException.ServerException(
                code,
                "Terlalu banyak permintaan. Tunggu beberapa saat sebelum mencoba lagi."
            )
            in 500..599 -> AppException.ServerException(
                code,
                errorMessage ?: getContextualServerErrorMessage(code, hasNetwork)
            )
            else -> AppException.ServerException(
                code,
                errorMessage ?: "Terjadi kesalahan server yang tidak dikenal (HTTP $code)."
            )
        }
    }

    /**
     * CONTEXTUAL ERROR MESSAGES
     */
    private fun getContextualValidationMessage(): String {
        return """
            Data tidak valid.
            
            Periksa:
            • Format email sudah benar
            • Password minimal 6 karakter
            • Semua field wajib diisi
            """.trimIndent()
    }

    private fun getContextualAuthMessage(hasNetwork: Boolean): String {
        return if (hasNetwork) {
            """
                Email atau password salah.
                
                Pastikan:
                • Email menggunakan format yang benar
                • Password sesuai dengan yang terdaftar
                • Caps Lock tidak aktif
                """.trimIndent()
        } else {
            "Tidak dapat memverifikasi login. Periksa koneksi internet Anda."
        }
    }

    private fun getContextual404Message(): String {
        return if (BuildConfig.DEBUG) {
            """
                Endpoint tidak ditemukan.
                
                Kemungkinan:
                • URL API salah
                • Route tidak tersedia di server
                • Server API belum dijalankan
                """.trimIndent()
        } else {
            "Data yang diminta tidak ditemukan."
        }
    }

    private fun getContextualServerErrorMessage(code: Int, hasNetwork: Boolean): String {
        val baseMessage = when (code) {
            500 -> "Server mengalami kesalahan internal"
            502 -> "Server gateway bermasalah"
            503 -> "Server sedang maintenance atau overload"
            504 -> "Server gateway timeout"
            else -> "Server mengalami masalah"
        }

        val suggestion = if (hasNetwork) {
            when (code) {
                500 -> "Coba lagi dalam beberapa menit. Jika masalah berlanjut, hubungi administrator."
                502, 503 -> "Server sedang bermasalah. Coba lagi nanti."
                504 -> "Koneksi ke server terlalu lambat. Coba lagi."
                else -> "Coba lagi nanti."
            }
        } else {
            "Periksa koneksi internet Anda."
        }

        return "$baseMessage. $suggestion"
    }

    /**
     * UNKNOWN EXCEPTION MAPPING
     */
    private fun mapUnknownException(throwable: Throwable, hasNetwork: Boolean): AppException {
        val contextMessage = if (hasNetwork) {
            "Terjadi kesalahan yang tidak terduga."
        } else {
            "Terjadi kesalahan. Periksa koneksi internet Anda."
        }

        return AppException.UnknownException(
            "$contextMessage (${throwable.javaClass.simpleName})",
            throwable
        )
    }

    /**
     * ERROR MESSAGE PARSING (enhanced from original)
     */
    private fun parseErrorMessage(httpException: HttpException): String? {
        return try {
            val errorBody = httpException.response()?.errorBody()?.string()
            if (!errorBody.isNullOrBlank()) {
                // Try multiple JSON patterns
                val patterns = listOf(
                    "\"message\"\\s*:\\s*\"([^\"]+)\"".toRegex(),
                    "\"error\"\\s*:\\s*\"([^\"]+)\"".toRegex(),
                    "\"detail\"\\s*:\\s*\"([^\"]+)\"".toRegex()
                )

                for (pattern in patterns) {
                    val matchResult = pattern.find(errorBody)
                    if (matchResult != null) {
                        return matchResult.groups[1]?.value
                    }
                }
            }
            null
        } catch (e: Exception) {
            null
        }
    }

    /**
     * DEBUG HELPERS
     */
    fun getNetworkDebugInfo(): String {
        val hasNetwork = isNetworkAvailable()

        return """
            Network Debug Info:
            - Network Available: $hasNetwork
            - Development Mode: ${BuildConfig.DEBUG}
            - Target Server: $DEVELOPMENT_SERVER_URL
            - Build Type: ${BuildConfig.BUILD_TYPE}
            """.trimIndent()
    }

    fun getExceptionMappingDebug(throwable: Throwable): String {
        val hasNetwork = isNetworkAvailable()
        val mappedException = mapToAppException(throwable)

        return """
            Exception Mapping Debug:
            - Original: ${throwable::class.simpleName}
            - Original Message: ${throwable.message}
            - Mapped: ${mappedException::class.simpleName}
            - Final Message: ${mappedException.message}
            - Network Context: $hasNetwork
            """.trimIndent()
    }
}
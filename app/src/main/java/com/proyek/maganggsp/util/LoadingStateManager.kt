// File: app/src/main/java/com/proyek/maganggsp/util/LoadingStateManager.kt
package com.proyek.maganggsp.util

import android.view.View
import androidx.core.view.isVisible
import com.facebook.shimmer.ShimmerFrameLayout
import com.proyek.maganggsp.util.exceptions.AppException

/**
 * MODULAR: Loading state management utilities
 * Extracted from unified AppUtils.kt for better modularity
 */
object LoadingStateManager {

    /**
     * Primary extension for standard shimmer + content + empty view pattern
     * Works with standardized shimmer ID: "standardShimmerLayout"
     */
    fun <T> Resource<T>.applyToStandardLoadingViews(
        shimmerView: ShimmerFrameLayout,
        contentView: View,
        emptyView: View? = null
    ): Resource<T> {
        when (this) {
            is Resource.Loading -> {
                shimmerView.startShimmer()
                shimmerView.isVisible = true
                contentView.isVisible = false
                emptyView?.isVisible = false
            }
            is Resource.Success -> {
                shimmerView.stopShimmer()
                shimmerView.isVisible = false

                val hasData = when (data) {
                    is List<*> -> data.isNotEmpty()
                    is String -> data.isNotBlank()
                    null -> false
                    else -> true
                }

                if (hasData) {
                    contentView.isVisible = true
                    emptyView?.isVisible = false
                } else {
                    contentView.isVisible = false
                    emptyView?.isVisible = true
                }
            }
            is Resource.Error -> {
                shimmerView.stopShimmer()
                shimmerView.isVisible = false
                contentView.isVisible = false
                emptyView?.isVisible = false
            }
            is Resource.Empty -> {
                shimmerView.stopShimmer()
                shimmerView.isVisible = false
                contentView.isVisible = false
                emptyView?.isVisible = true
            }
        }
        return this
    }

    /**
     * BACKWARD COMPATIBILITY: Support for existing applyToLoadingViews calls
     */
    fun <T> Resource<T>.applyToLoadingViews(
        shimmerView: View,
        contentView: View,
        emptyView: View? = null
    ): Resource<T> {
        when (this) {
            is Resource.Loading -> {
                startShimmerSafely(shimmerView)
                shimmerView.isVisible = true
                contentView.isVisible = false
                emptyView?.isVisible = false
            }
            is Resource.Success -> {
                stopShimmerSafely(shimmerView)
                shimmerView.isVisible = false

                val hasData = when (data) {
                    is List<*> -> data.isNotEmpty()
                    is String -> data.isNotBlank()
                    null -> false
                    else -> true
                }

                if (hasData) {
                    contentView.isVisible = true
                    emptyView?.isVisible = false
                } else {
                    contentView.isVisible = false
                    emptyView?.isVisible = true
                }
            }
            is Resource.Error -> {
                stopShimmerSafely(shimmerView)
                shimmerView.isVisible = false
                contentView.isVisible = false
                emptyView?.isVisible = false
            }
            is Resource.Empty -> {
                stopShimmerSafely(shimmerView)
                shimmerView.isVisible = false
                contentView.isVisible = false
                emptyView?.isVisible = true
            }
        }
        return this
    }

    /**
     * ENHANCED: Dual shimmer support (like DetailLoketActivity with card + mutations shimmer)
     */
    fun <T> Resource<T>.applyToDualLoadingViews(
        primaryShimmer: ShimmerFrameLayout,
        primaryContent: View,
        secondaryShimmer: ShimmerFrameLayout? = null,
        secondaryContent: View? = null,
        emptyView: View? = null
    ): Resource<T> {
        when (this) {
            is Resource.Loading -> {
                // Primary loading
                primaryShimmer.startShimmer()
                primaryShimmer.isVisible = true
                primaryContent.isVisible = false

                // Secondary loading (if exists)
                secondaryShimmer?.startShimmer()
                secondaryShimmer?.isVisible = true
                secondaryContent?.isVisible = false

                emptyView?.isVisible = false
            }
            is Resource.Success -> {
                // Stop all shimmers
                primaryShimmer.stopShimmer()
                primaryShimmer.isVisible = false
                secondaryShimmer?.stopShimmer()
                secondaryShimmer?.isVisible = false

                // Show content
                primaryContent.isVisible = true
                secondaryContent?.isVisible = true
                emptyView?.isVisible = false
            }
            is Resource.Error -> {
                // Stop all shimmers
                primaryShimmer.stopShimmer()
                primaryShimmer.isVisible = false
                secondaryShimmer?.stopShimmer()
                secondaryShimmer?.isVisible = false

                // Hide content
                primaryContent.isVisible = false
                secondaryContent?.isVisible = false
                emptyView?.isVisible = false
            }
            is Resource.Empty -> {
                // Stop all shimmers
                primaryShimmer.stopShimmer()
                primaryShimmer.isVisible = false
                secondaryShimmer?.stopShimmer()
                secondaryShimmer?.isVisible = false

                // Show empty state
                primaryContent.isVisible = false
                secondaryContent?.isVisible = false
                emptyView?.isVisible = true
            }
        }
        return this
    }

    /**
     * UTILITY: Safe shimmer operations that handle different view types
     */
    private fun startShimmerSafely(view: View) {
        when (view) {
            is ShimmerFrameLayout -> view.startShimmer()
            // Could extend to support other shimmer libraries
        }
    }

    private fun stopShimmerSafely(view: View) {
        when (view) {
            is ShimmerFrameLayout -> view.stopShimmer()
            // Could extend to support other shimmer libraries
        }
    }
}

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

// File: app/src/main/java/com/proyek/maganggsp/util/LoggingUtils.kt
package com.proyek.maganggsp.util

import android.util.Log
import com.proyek.maganggsp.BuildConfig
import com.proyek.maganggsp.util.exceptions.AppException

/**
 * MODULAR: Logging utilities
 * Extracted from unified AppUtils.kt for better modularity
 */
object LoggingUtils {

    private const val TAG_PREFIX = "GesPay"

    /**
     * Debug log - only shows in debug builds
     */
    fun logDebug(tag: String, message: String) {
        if (BuildConfig.DEBUG) {
            Log.d("$TAG_PREFIX-$tag", message)
        }
    }

    /**
     * Info log
     */
    fun logInfo(tag: String, message: String) {
        Log.i("$TAG_PREFIX-$tag", message)
    }

    /**
     * Warning log
     */
    fun logWarning(tag: String, message: String) {
        Log.w("$TAG_PREFIX-$tag", message)
    }

    /**
     * Error log with exception
     */
    fun logError(tag: String, message: String, exception: Throwable? = null) {
        if (exception != null) {
            Log.e("$TAG_PREFIX-$tag", "$message: ${exception.message}", exception)
        } else {
            Log.e("$TAG_PREFIX-$tag", message)
        }
    }

    /**
     * Error log with AppException
     */
    fun logError(tag: String, message: String, exception: AppException) {
        Log.e("$TAG_PREFIX-$tag", "$message: ${exception.message}", exception)
    }

    /**
     * Network request log
     */
    fun logNetworkRequest(tag: String, method: String, url: String) {
        if (BuildConfig.DEBUG) {
            Log.d("$TAG_PREFIX-Network-$tag", "$method -> $url")
        }
    }

    /**
     * Network response log
     */
    fun logNetworkResponse(tag: String, url: String, responseCode: Int) {
        if (BuildConfig.DEBUG) {
            Log.d("$TAG_PREFIX-Network-$tag", "$url <- HTTP $responseCode")
        }
    }

    /**
     * Navigation log
     */
    fun logNavigation(tag: String, from: String, to: String, args: String = "") {
        if (BuildConfig.DEBUG) {
            val argsText = if (args.isNotEmpty()) " with args: $args" else ""
            Log.d("$TAG_PREFIX-Nav-$tag", "$from -> $to$argsText")
        }
    }

    /**
     * Performance log
     */
    fun logPerformance(tag: String, operation: String, durationMs: Long) {
        if (BuildConfig.DEBUG) {
            Log.d("$TAG_PREFIX-Perf-$tag", "$operation took ${durationMs}ms")
        }
    }
}
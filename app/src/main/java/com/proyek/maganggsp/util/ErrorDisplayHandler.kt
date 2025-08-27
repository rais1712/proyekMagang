// File: app/src/main/java/com/proyek/maganggsp/util/ErrorDisplayHandler.kt
package com.proyek.maganggsp.util

import android.content.Context
import android.widget.Toast
import com.google.android.material.snackbar.Snackbar
import android.view.View
import android.widget.TextView
import com.proyek.maganggsp.util.exceptions.AppException

/**
 * CENTRALIZED ERROR DISPLAY HANDLER
 *
 * Replaces scattered Toast.makeText() calls across:
 * - LoginActivity
 * - HomeFragment
 * - HistoryFragment
 * - DetailLoketActivity
 * - All other activities/fragments
 *
 * Provides consistent, user-friendly error messaging
 */
object ErrorDisplayHandler {

    /**
     * ERROR TYPES for different display methods
     */
    enum class ErrorType {
        NETWORK,        // Network-related errors (show with retry option)
        VALIDATION,     // Input validation errors (show briefly)
        AUTHENTICATION, // Login/session errors (show persistently)
        SERVER,         // Server errors (show with info)
        UNKNOWN        // Unknown errors (show briefly)
    }

    /**
     * PRIMARY ERROR DISPLAY - Toast
     * Replaces all Toast.makeText() calls
     */
    fun showError(context: Context, message: String, type: ErrorType = ErrorType.UNKNOWN) {
        val duration = when (type) {
            ErrorType.AUTHENTICATION,
            ErrorType.NETWORK -> Toast.LENGTH_LONG
            else -> Toast.LENGTH_SHORT
        }

        val enhancedMessage = enhanceErrorMessage(message, type)
        Toast.makeText(context, enhancedMessage, duration).show()
    }

    /**
     * ENHANCED ERROR DISPLAY with AppException handling
     */
    fun showError(context: Context, exception: AppException) {
        val type = mapExceptionToType(exception)
        val message = exception.message
        showError(context, message, type)
    }

    /**
     * RETRYABLE ERROR DISPLAY - Snackbar with retry action
     * For network errors and other retryable failures
     */
    fun showRetryableError(
        context: Context,
        view: View,
        message: String,
        onRetry: () -> Unit
    ) {
        val enhancedMessage = enhanceErrorMessage(message, ErrorType.NETWORK)

        Snackbar.make(view, enhancedMessage, Snackbar.LENGTH_INDEFINITE)
            .setAction("COBA LAGI") { onRetry() }
            .show()
    }

    /**
     * RESOURCE ERROR HANDLER - Direct Resource<*> handling
     * For use with Resource.Error states
     */
    fun handleResourceError(
        context: Context,
        resource: Resource.Error<*>,
        rootView: View? = null
    ) {
        val exception = resource.exception
        val type = mapExceptionToType(exception)

        when (type) {
            ErrorType.NETWORK -> {
                if (rootView != null) {
                    showRetryableError(context, rootView, exception.message) {
                        // Retry will be handled by caller
                    }
                } else {
                    showError(context, exception.message, type)
                }
            }
            else -> {
                showError(context, exception.message, type)
            }
        }
    }

    /**
     * Integrated error handling dengan EmptyStateHandler
     */
    fun handleErrorWithEmptyState(
        context: Context,
        view: View,
        emptyStateTextView: TextView?,
        exception: AppException,
        onRetry: (() -> Unit)? = null
    ) {
        val errorType = mapExceptionToType(exception)
        val message = enhanceErrorMessage(exception.message, errorType)

        // Update empty state jika TextView tersedia
        emptyStateTextView?.let {
            EmptyStateHandler.applyErrorState(it, message, errorType)
        }

        // Show error message
        when (errorType) {
            ErrorType.NETWORK -> {
                onRetry?.let {
                    showRetryableError(context, view, message, it)
                } ?: showError(context, message, errorType)
            }
            else -> showError(context, message, errorType)
        }
    }

    /**
     * CONTEXT-SPECIFIC ERROR MESSAGES
     * Enhanced messages based on error type and context
     */
    private fun enhanceErrorMessage(message: String, type: ErrorType): String {
        return when (type) {
            ErrorType.NETWORK -> {
                when {
                    message.contains("connection", ignoreCase = true) ->
                        "⚠️ Koneksi internet bermasalah. Periksa jaringan Anda."
                    message.contains("timeout", ignoreCase = true) ->
                        "⏱️ Koneksi timeout. Server mungkin sedang lambat."
                    message.contains("server", ignoreCase = true) ->
                        "🔧 Server tidak dapat dijangkau. Pastikan server development berjalan."
                    else -> "🌐 $message"
                }
            }
            ErrorType.AUTHENTICATION -> {
                when {
                    message.contains("expired", ignoreCase = true) ->
                        "🔐 Sesi login telah berakhir. Silakan login kembali."
                    message.contains("unauthorized", ignoreCase = true) ->
                        "❌ Email atau password salah. Coba lagi."
                    else -> "🔑 $message"
                }
            }
            ErrorType.VALIDATION -> {
                when {
                    message.contains("email", ignoreCase = true) ->
                        "📧 $message"
                    message.contains("password", ignoreCase = true) ->
                        "🔐 $message"
                    else -> "✏️ $message"
                }
            }
            ErrorType.SERVER -> {
                when {
                    message.contains("500", ignoreCase = true) ->
                        "🔧 Server sedang bermasalah. Coba lagi nanti."
                    message.contains("404", ignoreCase = true) ->
                        "🔍 Data tidak ditemukan di server."
                    else -> "⚠️ $message"
                }
            }
            ErrorType.UNKNOWN -> message
        }
    }

    /**
     * EXCEPTION TO ERROR TYPE MAPPING
     * Maps AppException types to ErrorType for appropriate handling
     */
    private fun mapExceptionToType(exception: AppException): ErrorType {
        return when (exception) {
            is AppException.NetworkException -> ErrorType.NETWORK
            is AppException.AuthenticationException -> ErrorType.AUTHENTICATION
            is AppException.ValidationException -> ErrorType.VALIDATION
            is AppException.ServerException -> ErrorType.SERVER
            is AppException.UnauthorizedException -> ErrorType.AUTHENTICATION
            else -> ErrorType.UNKNOWN
        }
    }

    /**
     * MIGRATION HELPERS
     * For replacing existing Toast calls
     */

    // LoginActivity migration
    fun showLoginError(context: Context, message: String) {
        showError(context, message, ErrorType.AUTHENTICATION)
    }

    // Network error migration
    fun showNetworkError(context: Context, message: String) {
        showError(context, message, ErrorType.NETWORK)
    }

    // Validation error migration
    fun showValidationError(context: Context, message: String) {
        showError(context, message, ErrorType.VALIDATION)
    }

    // Generic error migration (most common case)
    fun showGenericError(context: Context, message: String) {
        showError(context, message, ErrorType.UNKNOWN)
    }

    /**
     * BATCH ERROR DISPLAY
     * For displaying multiple errors at once
     */
    fun showErrors(context: Context, errors: List<String>, type: ErrorType = ErrorType.UNKNOWN) {
        if (errors.isEmpty()) return

        val combinedMessage = when {
            errors.size == 1 -> errors.first()
            errors.size <= 3 -> errors.joinToString("\n• ", "Kesalahan:\n• ")
            else -> "${errors.take(2).joinToString("\n• ", "Kesalahan:\n• ")}\n• dan ${errors.size - 2} lainnya..."
        }

        showError(context, combinedMessage, type)
    }

    /**
     * DEBUG HELPERS
     * For troubleshooting error display
     */
    fun getErrorDisplayDebugInfo(exception: AppException): String {
        val type = mapExceptionToType(exception)
        return """
        Error Display Debug:
        - Exception Type: ${exception::class.simpleName}
        - Mapped Error Type: $type
        - Original Message: ${exception.message}
        - Enhanced Message: ${enhanceErrorMessage(exception.message, type)}
        """.trimIndent()
    }
}
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
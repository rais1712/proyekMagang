// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/BaseRepository.kt

package com.proyek.maganggsp.data.repositoryImpl

import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import android.util.Log

/**
 * Base class untuk semua Repository Implementation
 * Menyediakan exception mapping dan error handling
 */
abstract class BaseRepository(
    protected val exceptionMapper: ExceptionMapper
) {
    companion object {
        private const val TAG = "BaseRepository"
    }

    /**
     * Handle dan map exceptions ke user-friendly messages
     */
    protected fun <T> handleException(
        exception: Exception,
        context: String = "Operation"
    ): Nothing {
        Log.e(TAG, "$context failed: ${exception.message}", exception)
        throw exceptionMapper.mapException(exception)
    }

    /**
     * Safe execute dengan automatic exception handling
     */
    protected suspend fun <T> executeSafely(
        operation: suspend () -> T
    ): T {
        return try {
            operation()
        } catch (e: Exception) {
            handleException(e, "Repository operation")
        }
    }
}

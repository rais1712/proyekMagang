// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/BaseRepository.kt
package com.proyek.maganggsp.data.repositoryImpl

import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import android.util.Log

abstract class BaseRepository(
    protected val exceptionMapper: ExceptionMapper
) {
    companion object {
        private const val TAG = "BaseRepository"
    }

    protected fun handleException(
        exception: Exception,
        context: String = "Operation"
    ): Nothing {
        Log.e(TAG, "$context failed: ${exception.message}", exception)
        throw exceptionMapper.toAppException(exception)
    }

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

// File: app/src/main/java/com/proyek/maganggsp/util/Resource.kt
package com.proyek.maganggsp.util

import com.proyek.maganggsp.util.exceptions.AppException

/**
 * FIXED: Unified Resource class dengan consistent constructor parameters
 * dan better type safety - Based on user's provided structure
 */
sealed class Resource<out T> {

    data class Success<T>(val data: T) : Resource<T>()

    data class Loading<T>(val data: T? = null) : Resource<T>()

    data class Error<T>(
        val exception: AppException,
        val data: T? = null
    ) : Resource<T>() {
        val message: String get() = exception.message
    }

    object Empty : Resource<Nothing>()
}

/**
 * Extension functions untuk memudahkan handling Resource
 */
inline fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> {
    if (this is Resource.Success) action(data)
    return this
}

inline fun <T> Resource<T>.onError(action: (AppException) -> Unit): Resource<T> {
    if (this is Resource.Error) action(exception)
    return this
}

inline fun <T> Resource<T>.onLoading(action: () -> Unit): Resource<T> {
    if (this is Resource.Loading) action()
    return this
}

/**
 * Map Resource<T> to Resource<R>
 */
inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> {
    return when (this) {
        is Resource.Success -> Resource.Success(transform(data))
        is Resource.Error -> Resource.Error(exception, data?.let(transform))
        is Resource.Loading -> Resource.Loading(data?.let(transform))
        is Resource.Empty -> Resource.Empty
    }
}

/**
 * Get data or default value
 */
fun <T> Resource<T>.getDataOrNull(): T? {
    return when (this) {
        is Resource.Success -> data
        is Resource.Error -> data
        is Resource.Loading -> data
        is Resource.Empty -> null
    }
}
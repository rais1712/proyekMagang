// File: app/src/main/java/com/proyek/maganggsp/util/Resource.kt
package com.proyek.maganggsp.util

import android.view.View
import androidx.core.view.isVisible
import com.facebook.shimmer.ShimmerFrameLayout
import com.proyek.maganggsp.util.exceptions.AppException

/**
 * ENHANCED: Unified Resource class dengan improved loading state management
 * Based on existing structure + new standardized loading extensions
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
 * ENHANCED: Standardized loading state management extensions
 * Handles all shimmer variations with consistent naming
 */

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
 * Automatically handles different shimmer view types
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

/**
 * EXISTING EXTENSIONS: Keep all existing Resource extensions for backward compatibility
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

inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> {
    return when (this) {
        is Resource.Success -> Resource.Success(transform(data))
        is Resource.Error -> Resource.Error(exception, data?.let(transform))
        is Resource.Loading -> Resource.Loading(data?.let(transform))
        is Resource.Empty -> Resource.Empty
    }
}

fun <T> Resource<T>.getDataOrNull(): T? {
    return when (this) {
        is Resource.Success -> data
        is Resource.Error -> data
        is Resource.Loading -> data
        is Resource.Empty -> null
    }
}

/**
 * NEW: Loading state helper functions
 */

fun <T> Resource<T>.isLoading(): Boolean = this is Resource.Loading

fun <T> Resource<T>.isSuccess(): Boolean = this is Resource.Success

fun <T> Resource<T>.isError(): Boolean = this is Resource.Error

fun <T> Resource<T>.isEmpty(): Boolean = this is Resource.Empty

/**
 * NEW: Data validation helpers
 */
fun <T> Resource<T>.hasData(): Boolean {
    val data = getDataOrNull()
    return when (data) {
        is List<*> -> data.isNotEmpty()
        is String -> data.isNotBlank()
        null -> false
        else -> true
    }
}

fun <T> Resource<T>.isEmptyData(): Boolean = !hasData()
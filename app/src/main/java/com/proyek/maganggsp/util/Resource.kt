// File: app/src/main/java/com/proyek/maganggsp/util/Resource.kt
package com.proyek.maganggsp.util

import android.view.View
import androidx.core.view.isVisible
import com.facebook.shimmer.ShimmerFrameLayout
import com.proyek.maganggsp.util.exceptions.AppException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.retry
import kotlin.math.pow

/**
 * ENHANCED: Unified Resource class dengan improved loading state management + Smart Retry
 * Based on existing structure + new standardized loading extensions + retry mechanisms
 */
sealed class Resource<out T> {

    data class Success<T>(val data: T) : Resource<T>()

    data class Loading<T>(val data: T? = null) : Resource<T>()

    data class Error<T>(
        val message: String,
        val data: T? = null
    ) : Resource<T>() {
        constructor(exception: AppException, data: T? = null) : this(exception.message, data)
    }

    class Empty<T> : Resource<T>()
}

// ============================================
// EXISTING EXTENSIONS (UNCHANGED)
// ============================================

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
        is Resource.Empty() -> {
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
        is Resource.Empty() -> {
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
        is Resource.Empty() -> {
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

// ============================================
// NEW: SMART RETRY MECHANISMS
// ============================================

/**
 * RETRY CONFIGURATION
 */
data class RetryConfig(
    val maxRetries: Int = 3,
    val initialDelayMs: Long = 1000L,
    val maxDelayMs: Long = 10000L,
    val backoffMultiplier: Double = 2.0,
    val retryableExceptions: Set<Class<out AppException>> = setOf(
        AppException.NetworkException::class.java,
        AppException.ServerException::class.java
    )
)

/**
 * SMART RETRY EXTENSION for Resource<T> flows
 */
fun <T> Flow<Resource<T>>.withSmartRetry(
    config: RetryConfig = RetryConfig()
): Flow<Resource<T>> = flow {
    var retryAttempt = 0

    collect { resource ->
        when (resource) {
            is Resource.Error -> {
                val shouldRetry = config.retryableExceptions.contains(resource.message::class.java) &&
                        retryAttempt < config.maxRetries

                if (shouldRetry) {
                    retryAttempt++
                    val delay = calculateDelay(retryAttempt, config)

                    emit(Resource.Error(
                        AppException.NetworkException("Mencoba ulang ($retryAttempt/${config.maxRetries})...")
                    ))

                    delay(delay)
                } else {
                    emit(resource)
                }
            }
            else -> emit(resource)
        }
    }
}.retry(config.maxRetries.toLong()) { cause ->
    when (cause) {
        is AppException.NetworkException -> true
        is AppException.ServerException -> cause.httpCode in 500..599
        else -> false
    }
}

/**
 * EXPONENTIAL BACKOFF CALCULATION
 */
private fun calculateDelay(attempt: Int, config: RetryConfig): Long {
    val delay = config.initialDelayMs * config.backoffMultiplier.pow(attempt - 1)
    return delay.toLong().coerceAtMost(config.maxDelayMs)
}

/**
 * RETRY STRATEGIES for different contexts
 */
object RetryStrategies {
    val API_CALLS = RetryConfig(maxRetries = 3, initialDelayMs = 1000L, backoffMultiplier = 2.0)
    val SEARCH_OPERATIONS = RetryConfig(maxRetries = 2, initialDelayMs = 500L, backoffMultiplier = 1.5)
    val DATA_LOADING = RetryConfig(maxRetries = 4, initialDelayMs = 2000L, maxDelayMs = 15000L)
    val USER_ACTIONS = RetryConfig(maxRetries = 2, initialDelayMs = 1000L, backoffMultiplier = 1.0)
}

/**
 * CONTEXT-SPECIFIC RETRY EXTENSIONS
 */
fun <T> Flow<Resource<T>>.withHomeRetry(): Flow<Resource<T>> = withSmartRetry(RetryStrategies.DATA_LOADING)
fun <T> Flow<Resource<T>>.withSearchRetry(): Flow<Resource<T>> = withSmartRetry(RetryStrategies.SEARCH_OPERATIONS)
fun <T> Flow<Resource<T>>.withDetailRetry(): Flow<Resource<T>> = withSmartRetry(RetryStrategies.API_CALLS)
fun <T> Flow<Resource<T>>.withHistoryRetry(): Flow<Resource<T>> = withSmartRetry(RetryStrategies.DATA_LOADING)
fun <T> Flow<Resource<T>>.withActionRetry(): Flow<Resource<T>> = withSmartRetry(RetryStrategies.USER_ACTIONS)

/**
 * MANUAL RETRY EXTENSION
 */
fun <T> Resource<T>.withRetry(retryAction: () -> Unit): Resource<T> {
    return when (this) {
        is Resource.Error -> this.also {
            // Store retry action in a companion map if needed
        }
        else -> this
    }
}

// ============================================
// EXISTING EXTENSIONS (UNCHANGED)
// ============================================

inline fun <T> Resource<T>.onSuccess(action: (T) -> Unit): Resource<T> {
    if (this is Resource.Success) action(data)
    return this
}

inline fun <T> Resource<T>.onError(action: (String) -> Unit): Resource<T> {
    if (this is Resource.Error) action(message)
    return this
}

inline fun <T> Resource<T>.onLoading(action: () -> Unit): Resource<T> {
    if (this is Resource.Loading) action()
    return this
}

inline fun <T, R> Resource<T>.map(transform: (T) -> R): Resource<R> {
    return when (this) {
        is Resource.Success -> Resource.Success(transform(data))
        is Resource.Error -> Resource.Error(message, data?.let(transform))
        is Resource.Loading -> Resource.Loading(data?.let(transform))
        is Resource.Empty() -> Resource.Empty()()
    }
}

fun <T> Resource<T>.getDataOrNull(): T? {
    return when (this) {
        is Resource.Success -> data
        is Resource.Error -> data
        is Resource.Loading -> data
        is Resource.Empty() -> null
    }
}

// Helper functions
fun <T> Resource<T>.isLoading(): Boolean = this is Resource.Loading
fun <T> Resource<T>.isSuccess(): Boolean = this is Resource.Success
fun <T> Resource<T>.isError(): Boolean = this is Resource.Error
fun <T> Resource<T>.isEmpty(): Boolean = this is Resource.Empty()

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
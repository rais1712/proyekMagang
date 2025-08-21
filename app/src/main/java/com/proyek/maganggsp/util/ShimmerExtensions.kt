// File: app/src/main/java/com/proyek/maganggsp/util/ShimmerExtensions.kt
package com.proyek.maganggsp.util

import android.view.View
import androidx.core.view.isVisible
import com.facebook.shimmer.ShimmerFrameLayout

/**
 * STANDARDIZED: Extension functions untuk unified shimmer/loading state handling
 * Menggantikan scattered shimmer management code di semua screens
 */

/**
 * Show loading state - start shimmer dan hide content
 */
fun ShimmerFrameLayout.showLoading() {
    isVisible = true
    startShimmer()
}

/**
 * Hide loading state - stop shimmer dan hide shimmer view
 */
fun ShimmerFrameLayout.hideLoading() {
    stopShimmer()
    isVisible = false
}

/**
 * Standardized loading state management untuk 3 common views:
 * - ShimmerFrameLayout (loading)
 * - Content View (success)
 * - Empty/Error View (empty/error states)
 */
fun manageLoadingState(
    shimmerView: ShimmerFrameLayout,
    contentView: View,
    emptyView: View? = null,
    state: LoadingState
) {
    when (state) {
        LoadingState.Loading -> {
            shimmerView.showLoading()
            contentView.isVisible = false
            emptyView?.isVisible = false
        }
        LoadingState.Success -> {
            shimmerView.hideLoading()
            contentView.isVisible = true
            emptyView?.isVisible = false
        }
        LoadingState.Empty -> {
            shimmerView.hideLoading()
            contentView.isVisible = false
            emptyView?.isVisible = true
        }
        LoadingState.Error -> {
            shimmerView.hideLoading()
            contentView.isVisible = false
            emptyView?.isVisible = false // Error akan ditampilkan via error banner
        }
    }
}

/**
 * Extension untuk Resource<T> to LoadingState mapping
 */
fun <T> Resource<T>.toLoadingState(): LoadingState {
    return when (this) {
        is Resource.Loading -> LoadingState.Loading
        is Resource.Success -> {
            // Check if data is empty list
            when (val data = this.data) {
                is List<*> -> if (data.isEmpty()) LoadingState.Empty else LoadingState.Success
                null -> LoadingState.Empty
                else -> LoadingState.Success
            }
        }
        is Resource.Error -> LoadingState.Error
        is Resource.Empty -> LoadingState.Empty
    }
}

/**
 * Enum untuk represent different loading states
 */
enum class LoadingState {
    Loading,
    Success,
    Empty,
    Error
}

/**
 * Convenience extension untuk langsung apply ke Resource<T>
 */
fun <T> Resource<T>.applyToLoadingViews(
    shimmerView: ShimmerFrameLayout,
    contentView: View,
    emptyView: View? = null
) {
    manageLoadingState(shimmerView, contentView, emptyView, this.toLoadingState())
}
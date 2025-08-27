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


/**
 * UNIFIED LOADING STATE MANAGEMENT
 *
 * Consolidates all shimmer variants into single standard approach:
 * - standardShimmerLayout -> applyStandardLoading()
 * - shimmerLayout -> applyStandardLoading()
 * - mutationsShimmerLayout -> applyStandardLoading()
 *
 * TARGET: Single loading behavior across HomeFragment, DetailLoket, HistoryFragment, Monitor tabs
 */
object LoadingStateManager {

    /**
     * STANDARD LOADING APPLICATION
     * Replaces all shimmer variants with unified behavior
     */
    fun applyStandardLoading(
        shimmerView: ShimmerFrameLayout,
        contentView: View,
        emptyView: View? = null,
        resource: Resource<*>
    ) {
        when (resource) {
            is Resource.Loading -> {
                // Start shimmer and show loading state
                shimmerView.startShimmer()
                shimmerView.isVisible = true
                contentView.isVisible = false
                emptyView?.isVisible = false
            }
            is Resource.Success -> {
                // Stop shimmer and evaluate content
                shimmerView.stopShimmer()
                shimmerView.isVisible = false

                val hasData = evaluateDataAvailability(resource.data)

                if (hasData) {
                    contentView.isVisible = true
                    emptyView?.isVisible = false
                } else {
                    contentView.isVisible = false
                    emptyView?.isVisible = true
                }
            }
            is Resource.Error -> {
                // Stop shimmer and hide all content for error display
                shimmerView.stopShimmer()
                shimmerView.isVisible = false
                contentView.isVisible = false
                emptyView?.isVisible = false
                // Error handling will be done by ErrorDisplayHandler
            }
            is Resource.Empty -> {
                // Stop shimmer and show empty state
                shimmerView.stopShimmer()
                shimmerView.isVisible = false
                contentView.isVisible = false
                emptyView?.isVisible = true
            }
        }
    }

    /**
     * DUAL LOADING SUPPORT
     * For DetailLoketActivity with card + mutations shimmer
     */
    fun applyDualStandardLoading(
        primaryShimmer: ShimmerFrameLayout,
        primaryContent: View,
        secondaryShimmer: ShimmerFrameLayout? = null,
        secondaryContent: View? = null,
        emptyView: View? = null,
        resource: Resource<*>
    ) {
        when (resource) {
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

                // Show content based on data availability
                val hasData = evaluateDataAvailability(resource.data)

                if (hasData) {
                    primaryContent.isVisible = true
                    secondaryContent?.isVisible = true
                    emptyView?.isVisible = false
                } else {
                    primaryContent.isVisible = false
                    secondaryContent?.isVisible = false
                    emptyView?.isVisible = true
                }
            }
            is Resource.Error -> {
                // Stop all shimmers
                primaryShimmer.stopShimmer()
                primaryShimmer.isVisible = false
                secondaryShimmer?.stopShimmer()
                secondaryShimmer?.isVisible = false

                // Hide content, show error via ErrorDisplayHandler
                primaryContent.isVisible = false
                secondaryContent?.isVisible = false
                emptyView?.isVisible = false
            }
            is Resource.Empty -> {
                // Stop all shimmers and show empty
                primaryShimmer.stopShimmer()
                primaryShimmer.isVisible = false
                secondaryShimmer?.stopShimmer()
                secondaryShimmer?.isVisible = false

                primaryContent.isVisible = false
                secondaryContent?.isVisible = false
                emptyView?.isVisible = true
            }
        }
    }

    /**
     * SMART DATA EVALUATION
     * Determines if resource contains meaningful data
     */
    private fun evaluateDataAvailability(data: Any?): Boolean {
        return when (data) {
            is List<*> -> data.isNotEmpty()
            is String -> data.isNotBlank()
            null -> false
            else -> true
        }
    }

    /**
     * MIGRATION HELPERS
     * For updating existing code to use standard loading
     */

    // HomeFragment migration
    fun applyToHomeFragment(
        shimmerView: ShimmerFrameLayout,
        recyclerView: View,
        emptyView: View,
        resource: Resource<List<*>>
    ) = applyStandardLoading(shimmerView, recyclerView, emptyView, resource)

    // HistoryFragment migration
    fun applyToHistoryFragment(
        shimmerView: ShimmerFrameLayout,
        recyclerView: View,
        emptyView: View,
        resource: Resource<List<*>>
    ) = applyStandardLoading(shimmerView, recyclerView, emptyView, resource)

    // DetailLoket card migration
    fun applyToDetailLoketCard(
        shimmerView: ShimmerFrameLayout,
        cardView: View,
        resource: Resource<*>
    ) = applyStandardLoading(shimmerView, cardView, null, resource)

    // Monitor tabs migration
    fun applyToMonitorTab(
        shimmerView: ShimmerFrameLayout,
        recyclerView: View,
        emptyView: View,
        resource: Resource<List<*>>
    ) = applyStandardLoading(shimmerView, recyclerView, emptyView, resource)

    /**
     * BACKWARD COMPATIBILITY
     * Support existing Resource extensions during transition period
     */
    fun <T> Resource<T>.applyStandardized(
        shimmerView: ShimmerFrameLayout,
        contentView: View,
        emptyView: View? = null
    ): Resource<T> {
        applyStandardLoading(shimmerView, contentView, emptyView, this)
        return this
    }

    /**
     * VALIDATION HELPERS
     * For debugging loading state applications
     */
    fun validateLoadingSetup(
        shimmerView: ShimmerFrameLayout?,
        contentView: View?,
        emptyView: View?
    ): List<String> {
        val issues = mutableListOf<String>()

        if (shimmerView == null) issues.add("ShimmerFrameLayout is null")
        if (contentView == null) issues.add("Content view is null")
        // emptyView can be null (optional)

        return issues
    }

    /**
     * ENHANCED EMPTY STATE INTEGRATION
     * Works with EmptyStateHandler for contextual messages
     */
    fun applyWithContextualEmptyState(
        shimmerView: ShimmerFrameLayout,
        contentView: View,
        emptyView: android.widget.TextView,
        resource: Resource<*>,
        context: String,
        isSearchMode: Boolean = false,
        searchQuery: String = ""
    ) {
        applyStandardLoading(shimmerView, contentView, emptyView, resource)

        // Apply contextual empty state if needed
        if (resource is Resource.Success && !evaluateDataAvailability(resource.data)) {
            val emptyStateType = EmptyStateHandler.getSmartEmptyState(
                context = context,
                itemCount = 0,
                isSearchMode = isSearchMode,
                searchQuery = searchQuery,
                hasNetworkConnection = true // TODO: Add actual network check
            )

            EmptyStateHandler.applyEmptyState(emptyView, emptyStateType, true)
        }
    }

    /**
     * DEBUG INFO
     * For troubleshooting loading states
     */
    fun getLoadingStateDebugInfo(
        shimmerView: ShimmerFrameLayout,
        contentView: View,
        emptyView: View?
    ): String {
        return """
        Loading State Debug:
        - Shimmer visible: ${shimmerView.isVisible}
        - Shimmer animating: ${shimmerView.isShimmerVisible}
        - Content visible: ${contentView.isVisible}
        - Empty visible: ${emptyView?.isVisible ?: "N/A"}
        """.trimIndent()
    }
}
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
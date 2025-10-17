// File: app/src/main/java/com/proyek/maganggsp/util/LoadingStateManager.kt
package com.proyek.maganggsp.util

import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.facebook.shimmer.ShimmerFrameLayout

/**
 * Helper class untuk manage loading states
 * Modular replacement untuk AppUtils loading functions
 */
object LoadingStateManager {

    /**
     * Show/hide loading state
     */
    fun handleLoadingState(
        isLoading: Boolean,
        progressBar: ProgressBar?,
        content: View?,
        shimmer: ShimmerFrameLayout? = null
    ) {
        if (isLoading) {
            showLoading(progressBar, shimmer)
            hideContent(content)
        } else {
            hideLoading(progressBar, shimmer)
            showContent(content)
        }
    }

    /**
     * Show loading indicators
     */
    private fun showLoading(progressBar: ProgressBar?, shimmer: ShimmerFrameLayout?) {
        progressBar?.visibility = View.VISIBLE
        shimmer?.let {
            it.visibility = View.VISIBLE
            it.startShimmer()
        }
    }

    /**
     * Hide loading indicators
     */
    private fun hideLoading(progressBar: ProgressBar?, shimmer: ShimmerFrameLayout?) {
        progressBar?.visibility = View.GONE
        shimmer?.let {
            it.stopShimmer()
            it.visibility = View.GONE
        }
    }

    /**
     * Show content
     */
    private fun showContent(content: View?) {
        content?.visibility = View.VISIBLE
    }

    /**
     * Hide content
     */
    private fun hideContent(content: View?) {
        content?.visibility = View.GONE
    }

    /**
     * Handle empty state
     */
    fun handleEmptyState(
        isEmpty: Boolean,
        emptyView: View?,
        content: View?
    ) {
        if (isEmpty) {
            emptyView?.visibility = View.VISIBLE
            content?.visibility = View.GONE
        } else {
            emptyView?.visibility = View.GONE
            content?.visibility = View.VISIBLE
        }
    }

    /**
     * Handle RecyclerView loading state
     */
    fun handleRecyclerViewState(
        recyclerView: RecyclerView?,
        isLoading: Boolean,
        isEmpty: Boolean,
        shimmer: ShimmerFrameLayout?,
        emptyView: View?
    ) {
        when {
            isLoading -> {
                recyclerView?.visibility = View.GONE
                emptyView?.visibility = View.GONE
                shimmer?.let {
                    it.visibility = View.VISIBLE
                    it.startShimmer()
                }
            }
            isEmpty -> {
                recyclerView?.visibility = View.GONE
                emptyView?.visibility = View.VISIBLE
                shimmer?.let {
                    it.stopShimmer()
                    it.visibility = View.GONE
                }
            }
            else -> {
                recyclerView?.visibility = View.VISIBLE
                emptyView?.visibility = View.GONE
                shimmer?.let {
                    it.stopShimmer()
                    it.visibility = View.GONE
                }
            }
        }
    }
}

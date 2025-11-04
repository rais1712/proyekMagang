package com.proyek.maganggsp.util

import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.facebook.shimmer.ShimmerFrameLayout

object UiStateHelper {
    
    fun <T> handleLoadingState(
        resource: Resource<T>,
        shimmerView: View,
        contentView: View,
        emptyView: View? = null
    ) {
        when (resource) {
            is Resource.Loading -> {
                if (shimmerView is ShimmerFrameLayout) {
                    shimmerView.startShimmer()
                }
                shimmerView.isVisible = true
                contentView.isVisible = false
                emptyView?.isVisible = false
            }
            is Resource.Success -> {
                if (shimmerView is ShimmerFrameLayout) {
                    shimmerView.stopShimmer()
                }
                shimmerView.isVisible = false
                
                val hasData = when (val data = resource.data) {
                    is List<*> -> data.isNotEmpty()
                    is String -> data.isNotBlank()
                    null -> false
                    else -> true
                }
                
                contentView.isVisible = hasData
                emptyView?.isVisible = !hasData
            }
            is Resource.Error -> {
                if (shimmerView is ShimmerFrameLayout) {
                    shimmerView.stopShimmer()
                }
                shimmerView.isVisible = false
                contentView.isVisible = false
                emptyView?.isVisible = false
            }
            is Resource.Empty() -> {
                if (shimmerView is ShimmerFrameLayout) {
                    shimmerView.stopShimmer()
                }
                shimmerView.isVisible = false
                contentView.isVisible = false
                emptyView?.isVisible = true
            }
        }
    }
    
    fun <T> handleDualLoadingState(
        resource: Resource<T>,
        primaryShimmer: ShimmerFrameLayout,
        primaryContent: View,
        secondaryShimmer: ShimmerFrameLayout? = null,
        secondaryContent: View? = null,
        emptyView: View? = null
    ) {
        when (resource) {
            is Resource.Loading -> {
                primaryShimmer.startShimmer()
                primaryShimmer.isVisible = true
                primaryContent.isVisible = false
                
                secondaryShimmer?.startShimmer()
                secondaryShimmer?.isVisible = true
                secondaryContent?.isVisible = false
                
                emptyView?.isVisible = false
            }
            is Resource.Success -> {
                primaryShimmer.stopShimmer()
                primaryShimmer.isVisible = false
                secondaryShimmer?.stopShimmer()
                secondaryShimmer?.isVisible = false
                
                primaryContent.isVisible = true
                secondaryContent?.isVisible = true
                emptyView?.isVisible = false
            }
            is Resource.Error -> {
                primaryShimmer.stopShimmer()
                primaryShimmer.isVisible = false
                secondaryShimmer?.stopShimmer()
                secondaryShimmer?.isVisible = false
                
                primaryContent.isVisible = false
                secondaryContent?.isVisible = false
                emptyView?.isVisible = false
            }
            is Resource.Empty() -> {
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
    
    fun applyEmptyState(textView: TextView, context: String, itemCount: Int) {
        textView.text = when {
            itemCount == 0 -> "Tidak ada data $context"
            else -> "$itemCount $context ditemukan"
        }
    }
}

// File: app/src/main/java/com/proyek/maganggsp/util/EmptyStateHandler.kt
package com.proyek.maganggsp.util

import android.content.Context
import android.widget.TextView
import androidx.core.view.isVisible
import com.proyek.maganggsp.R

/**
 * ENHANCED: Centralized empty state management dengan contextual messages
 * Provides consistent empty state experience across all screens
 */
object EmptyStateHandler {

    /**
     * Empty state types untuk different contexts
     */
    sealed class EmptyStateType {
        // Home Fragment States
        object NoRecentHistory : EmptyStateType()
        data class NoSearchResults(val query: String) : EmptyStateType()
        object SearchHint : EmptyStateType()

        // History Fragment States
        object NoHistoryFull : EmptyStateType()

        // Monitor Fragment States
        object NoFlaggedLokets : EmptyStateType()
        object NoBlockedLokets : EmptyStateType()

        // Detail Loket States
        object NoMutationsData : EmptyStateType()
        object MutationsLoadFailed : EmptyStateType()

        // Network States
        object NoConnection : EmptyStateType()
        data class LoadingState(val message: String) : EmptyStateType()
    }

    /**
     * Apply contextual empty state ke TextView
     */
    fun applyEmptyState(
        textView: TextView,
        emptyStateType: EmptyStateType,
        isVisible: Boolean = true
    ) {
        val context = textView.context
        textView.isVisible = isVisible

        if (!isVisible) return

        val message = getEmptyStateMessage(context, emptyStateType)
        textView.text = message

        // Apply appropriate styling based on state type
        applyEmptyStateStyle(textView, emptyStateType)
    }

    /**
     * Get contextual message berdasarkan empty state type
     */
    private fun getEmptyStateMessage(context: Context, emptyStateType: EmptyStateType): String {
        return when (emptyStateType) {
            // Home Fragment Messages
            is EmptyStateType.NoRecentHistory ->
                context.getString(R.string.empty_no_recent_history)

            is EmptyStateType.NoSearchResults ->
                context.getString(R.string.empty_no_search_results)

            is EmptyStateType.SearchHint ->
                context.getString(R.string.empty_search_hint)

            // History Fragment Messages
            is EmptyStateType.NoHistoryFull ->
                context.getString(R.string.empty_history_full)

            // Monitor Fragment Messages
            is EmptyStateType.NoFlaggedLokets ->
                context.getString(R.string.empty_flagged_lokets)

            is EmptyStateType.NoBlockedLokets ->
                context.getString(R.string.empty_blocked_lokets)

            // Detail Loket Messages
            is EmptyStateType.NoMutationsData ->
                context.getString(R.string.empty_mutations_no_data)

            is EmptyStateType.MutationsLoadFailed ->
                context.getString(R.string.empty_mutations_loading_failed)

            // Network Messages
            is EmptyStateType.NoConnection ->
                context.getString(R.string.search_no_connection)

            is EmptyStateType.LoadingState ->
                emptyStateType.message
        }
    }

    /**
     * Apply appropriate styling berdasarkan context
     */
    private fun applyEmptyStateStyle(textView: TextView, emptyStateType: EmptyStateType) {
        val context = textView.context

        when (emptyStateType) {
            is EmptyStateType.NoConnection -> {
                // Error state - bisa dikustomisasi dengan warna merah
                textView.setTextColor(
                    androidx.core.content.ContextCompat.getColor(context, R.color.text_secondary_gray)
                )
            }

            is EmptyStateType.SearchHint -> {
                // Hint state - lebih subtle
                textView.setTextColor(
                    androidx.core.content.ContextCompat.getColor(context, R.color.text_secondary_gray)
                )
            }

            else -> {
                // Default empty state color
                textView.setTextColor(
                    androidx.core.content.ContextCompat.getColor(context, R.color.text_secondary_gray)
                )
            }
        }

        // Center align untuk semua empty states
        textView.textAlignment = android.view.View.TEXT_ALIGNMENT_CENTER
    }

    /**
     * ENHANCED: Smart empty state detection dengan auto-suggestion
     */
    fun getSmartEmptyState(
        context: String, // context identifier
        itemCount: Int,
        isSearchMode: Boolean = false,
        searchQuery: String = "",
        hasNetworkConnection: Boolean = true
    ): EmptyStateType {

        return when {
            !hasNetworkConnection -> EmptyStateType.NoConnection

            context == "home" && isSearchMode -> {
                if (searchQuery.length < 3) EmptyStateType.SearchHint
                else EmptyStateType.NoSearchResults(searchQuery)
            }

            context == "home" && !isSearchMode && itemCount == 0 ->
                EmptyStateType.NoRecentHistory

            context == "history" && itemCount == 0 ->
                EmptyStateType.NoHistoryFull

            context == "flagged" && itemCount == 0 ->
                EmptyStateType.NoFlaggedLokets

            context == "blocked" && itemCount == 0 ->
                EmptyStateType.NoBlockedLokets

            context == "mutations" && itemCount == 0 ->
                EmptyStateType.NoMutationsData

            else -> EmptyStateType.NoRecentHistory // fallback
        }
    }

    /**
     * Quick helper untuk apply smart empty state
     */
    fun applySmartEmptyState(
        textView: TextView,
        context: String,
        itemCount: Int,
        isSearchMode: Boolean = false,
        searchQuery: String = "",
        hasNetworkConnection: Boolean = true
    ) {
        val emptyStateType = getSmartEmptyState(
            context, itemCount, isSearchMode, searchQuery, hasNetworkConnection
        )

        val shouldShow = itemCount == 0
        applyEmptyState(textView, emptyStateType, shouldShow)
    }

    /**
     * Apply empty state dengan error handling integration
     */
    fun applyErrorState(
        textView: TextView,
        errorMessage: String,
        errorType: ErrorDisplayHandler.ErrorType
    ) {
        textView.isVisible = true
        textView.text = errorMessage

        // Apply styling khusus untuk error
        when (errorType) {
            ErrorDisplayHandler.ErrorType.NETWORK -> {
                textView.setTextColor(
                    androidx.core.content.ContextCompat.getColor(
                        textView.context,
                        R.color.text_error // pastikan warna ini ada di colors.xml
                    )
                )
            }
            else -> applyEmptyStateStyle(textView, EmptyStateType.NoConnection)
        }
    }
}

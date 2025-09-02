// File: app/src/main/java/com/proyek/maganggsp/util/AppUtils.kt
package com.proyek.maganggsp.util

import android.content.Context
import android.view.View
import android.widget.TextView
import androidx.core.view.isVisible
import com.facebook.shimmer.ShimmerFrameLayout
import com.proyek.maganggsp.R
import com.proyek.maganggsp.util.exceptions.AppException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * CONSOLIDATED UTILITY CLASS
 * Merges: Formatters, ErrorDisplayHandler, EmptyStateHandler, LoadingStateHandler
 * Single comprehensive utility for all common operations
 */
object AppUtils {

    // ========================================
    // FORMATTING UTILITIES
    // ========================================

    /**
     * Format currency to Rupiah format
     */
    fun formatCurrency(amount: Long): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(amount)
    }

    /**
     * Format date string to readable format
     */
    fun formatDate(dateString: String): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(dateString)

            val readableFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
            readableFormat.format(date)
        } catch (e: Exception) {
            dateString // Return original if parsing fails
        }
    }

    // ========================================
    // ERROR HANDLING UTILITIES
    // ========================================

    /**
     * Show error message with appropriate handling
     */
    fun showError(context: Context, error: AppException) {
        val message = when (error) {
            is AppException.NetworkException -> "⚠️ ${error.message}"
            is AppException.AuthenticationException -> "🔐 ${error.message}"
            is AppException.ValidationException -> "✏️ ${error.message}"
            is AppException.ServerException -> "🔧 ${error.message}"
            else -> error.message
        }

        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_LONG).show()
    }

    /**
     * Show simple error message
     */
    fun showError(context: Context, message: String) {
        android.widget.Toast.makeText(context, message, android.widget.Toast.LENGTH_SHORT).show()
    }

    // ========================================
    // LOADING STATE UTILITIES
    // ========================================

    /**
     * Handle loading state for standard shimmer + content + empty pattern
     */
    fun handleLoadingState(
        shimmerView: ShimmerFrameLayout,
        contentView: View,
        emptyView: View? = null,
        resource: Resource<*>
    ) {
        when (resource) {
            is Resource.Loading -> {
                shimmerView.startShimmer()
                shimmerView.isVisible = true
                contentView.isVisible = false
                emptyView?.isVisible = false
            }
            is Resource.Success -> {
                shimmerView.stopShimmer()
                shimmerView.isVisible = false

                val hasData = evaluateDataAvailability(resource.data)
                contentView.isVisible = hasData
                emptyView?.isVisible = !hasData
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
    }

    /**
     * Evaluate if resource data is meaningful
     */
    private fun evaluateDataAvailability(data: Any?): Boolean {
        return when (data) {
            is List<*> -> data.isNotEmpty()
            is String -> data.isNotBlank()
            null -> false
            else -> true
        }
    }

    // ========================================
    // EMPTY STATE UTILITIES
    // ========================================

    /**
     * Apply contextual empty state messages
     */
    fun applyEmptyState(
        textView: TextView,
        context: String,
        itemCount: Int,
        isSearchMode: Boolean = false,
        searchQuery: String = ""
    ) {
        val message = when {
            context == "home" && isSearchMode -> {
                if (searchQuery.length < 3) "Ketik minimal 3 karakter untuk pencarian"
                else "Tidak ada hasil untuk \"$searchQuery\""
            }
            context == "home" && !isSearchMode && itemCount == 0 ->
                "Belum ada riwayat pencarian.\nCoba cari menggunakan nomor telepon."
            context == "history" && itemCount == 0 ->
                "Belum ada riwayat penelusuran.\nKembali ke Beranda untuk mulai mencari."
            context == "transactions" && itemCount == 0 ->
                "Belum ada riwayat transaksi untuk item ini."
            else -> "Tidak ada data tersedia"
        }

        textView.text = message
        textView.isVisible = itemCount == 0
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
    }

    // ========================================
    // VALIDATION UTILITIES
    // ========================================

    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validate PPID format (basic validation)
     */
    fun isValidPpid(ppid: String): Boolean {
        return ppid.isNotBlank() && ppid.length >= 5
    }

    // ========================================
    // NAVIGATION UTILITIES
    // ========================================

    /**
     * Safe navigation helper that handles exceptions
     */
    fun safeNavigate(action: () -> Unit, onError: (String) -> Unit = {}) {
        try {
            action()
        } catch (e: Exception) {
            onError("Navigasi gagal: ${e.message}")
        }
    }

    // ========================================
    // DEBUG UTILITIES
    // ========================================

    /**
     * Get debug info for troubleshooting
     */
    fun getDebugInfo(context: Context): String {
        return """
        📱 DEBUG INFO:
        - App Context: ${context.javaClass.simpleName}
        - Current Time: ${formatDate(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(Date()))}
        - Available Memory: ${Runtime.getRuntime().freeMemory() / 1024 / 1024} MB
        """.trimIndent()
    }
}
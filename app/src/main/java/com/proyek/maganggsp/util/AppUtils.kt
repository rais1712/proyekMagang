// File: app/src/main/java/com/proyek/maganggsp/util/AppUtils.kt - CONSOLIDATED & INDONESIAN
package com.proyek.maganggsp.util

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.facebook.shimmer.ShimmerFrameLayout
import com.proyek.maganggsp.R
import com.proyek.maganggsp.util.exceptions.AppException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * CONSOLIDATED UTILITY CLASS - Indonesian Messages
 * Menggabungkan: Formatters, ErrorDisplayHandler, EmptyStateHandler, LoadingStateHandler
 * Single comprehensive utility untuk semua operasi umum
 */
object AppUtils {

    // ========================================
    // FORMATTING UTILITIES - Indonesian Format
    // ========================================

    /**
     * Format mata uang ke format Rupiah
     */
    fun formatCurrency(amount: Long): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(amount)
    }

    /**
     * Format string tanggal ke format yang mudah dibaca
     */
    fun formatDate(dateString: String): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(dateString)

            val readableFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
            readableFormat.format(date!!)
        } catch (e: Exception) {
            // Fallback: coba format lain atau return original
            try {
                val fallbackFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val date = fallbackFormat.parse(dateString)
                val readableFormat = SimpleDateFormat("dd MMM yyyy", Locale("in", "ID"))
                readableFormat.format(date!!)
            } catch (e2: Exception) {
                dateString // Return original jika parsing gagal
            }
        }
    }

    /**
     * Format PPID untuk display
     */
    fun formatPpid(ppid: String): String {
        return when {
            ppid.length > 15 -> "${ppid.take(10)}...${ppid.takeLast(4)}"
            ppid.isBlank() -> "PPID tidak tersedia"
            else -> ppid
        }
    }

    // ========================================
    // ERROR HANDLING UTILITIES - Indonesian
    // ========================================

    /**
     * Tampilkan error message dengan handling yang tepat
     */
    fun showError(context: Context, error: AppException) {
        val message = mapExceptionToIndonesianMessage(error)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Tampilkan simple error message
     */
    fun showError(context: Context, message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    /**
     * Tampilkan success message
     */
    fun showSuccess(context: Context, message: String) {
        Toast.makeText(context, "✅ $message", Toast.LENGTH_SHORT).show()
    }

    private fun mapExceptionToIndonesianMessage(error: AppException): String {
        return when (error) {
            is AppException.NetworkException -> "🌐 Periksa koneksi internet Anda"
            is AppException.AuthenticationException -> "🔐 Sesi berakhir, silakan login kembali"
            is AppException.ValidationException -> "✏️ ${error.message}"
            is AppException.ServerException -> when (error.httpCode) {
                401 -> "🔐 Email atau password salah"
                404 -> "🔍 Data tidak ditemukan"
                500 -> "🔧 Server bermasalah, coba lagi nanti"
                else -> "🔧 Kesalahan server (${error.httpCode})"
            }
            else -> "⚠️ ${error.message}"
        }
    }

    // ========================================
    // LOADING STATE UTILITIES
    // ========================================

    /**
     * Handle loading state untuk pattern standar shimmer + content + empty
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
                emptyView?.isVisible = true
            }
            is Resource.Empty -> {
                shimmerView.stopShimmer()
                shimmerView.isVisible = false
                contentView.isVisible = false
                emptyView?.isVisible = true
            }
        }
    }

    private fun evaluateDataAvailability(data: Any?): Boolean {
        return when (data) {
            is List<*> -> data.isNotEmpty()
            is String -> data.isNotBlank()
            null -> false
            else -> true
        }
    }

    // ========================================
    // EMPTY STATE UTILITIES - Indonesian
    // ========================================

    /**
     * Apply contextual empty state messages dalam bahasa Indonesia
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
                "Belum ada receipt tersedia.\nTarik ke bawah untuk refresh."
            context == "transactions" && itemCount == 0 ->
                "Belum ada log transaksi.\nData akan muncul jika ada aktivitas."
            context == "profile" && itemCount == 0 ->
                "Informasi profil tidak tersedia."
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
     * Validasi format email
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Validasi format PPID
     */
    fun isValidPpid(ppid: String): Boolean {
        return ppid.isNotBlank() && ppid.length >= 5
    }

    /**
     * Validasi nomor referensi
     */
    fun isValidRefNumber(refNumber: String): Boolean {
        return refNumber.isNotBlank() && refNumber.length >= 3
    }

    // ========================================
    // NAVIGATION UTILITIES
    // ========================================

    /**
     * Safe navigation helper yang handle exceptions
     */
    fun safeNavigate(action: () -> Unit, onError: (String) -> Unit = {}) {
        try {
            action()
        } catch (e: Exception) {
            onError("Navigasi gagal: ${e.message}")
        }
    }

    // ========================================
    // PLACEHOLDER DATA UTILITIES
    // ========================================

    /**
     * Generate placeholder receipt untuk testing
     */
    fun createPlaceholderReceipt(ppid: String): com.proyek.maganggsp.domain.model.Receipt {
        return com.proyek.maganggsp.domain.model.Receipt(
            refNumber = "REF-${System.currentTimeMillis().toString().takeLast(6)}",
            idPelanggan = ppid,
            amount = (50000..500000L).random(),
            logged = formatDate("2024-01-15T10:30:00.000Z")
        )
    }

    /**
     * Generate placeholder transaction logs untuk testing
     */
    fun createPlaceholderTransactionLogs(ppid: String, count: Int = 5): List<com.proyek.maganggsp.domain.model.TransactionLog> {
        val transactions = mutableListOf<com.proyek.maganggsp.domain.model.TransactionLog>()
        var balance = 1000000L

        repeat(count) { index ->
            val amount = if (index % 3 == 0) -(10000..50000L).random() else (25000..100000L).random()
            balance += amount

            transactions.add(
                com.proyek.maganggsp.domain.model.TransactionLog(
                    tldRefnum = "TXN${String.format("%03d", index + 1)}-PLACEHOLDER",
                    tldPan = "1234****5678",
                    tldIdpel = ppid,
                    tldAmount = amount,
                    tldBalance = balance,
                    tldDate = "2024-01-${15 + index}T${10 + index}:30:00.000Z",
                    tldPpid = ppid
                )
            )
        }

        return transactions.reversed() // Terbaru dulu
    }

    // ========================================
    // DEBUG UTILITIES
    // ========================================

    /**
     * Get debug info untuk troubleshooting
     */
    fun getDebugInfo(context: Context): String {
        return """
        📱 DEBUG INFO GESPAY ADMIN:
        - Context: ${context.javaClass.simpleName}
        - Waktu: ${formatDate(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(Date()))}
        - Memory: ${Runtime.getRuntime().freeMemory() / 1024 / 1024} MB
        - Version: ${com.proyek.maganggsp.BuildConfig.VERSION_NAME}
        - Build Type: ${com.proyek.maganggsp.BuildConfig.BUILD_TYPE}
        - Base URL: ${com.proyek.maganggsp.BuildConfig.BASE_URL}
        """.trimIndent()
    }

    /**
     * Log dengan format konsisten
     */
    fun logInfo(tag: String, message: String) {
        android.util.Log.i(tag, "📋 $message")
    }

    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        android.util.Log.e(tag, "❌ $message", throwable)
    }

    fun logDebug(tag: String, message: String) {
        if (com.proyek.maganggsp.BuildConfig.DEBUG) {
            android.util.Log.d(tag, "🔍 $message")
        }
    }
}
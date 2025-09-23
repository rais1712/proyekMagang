// File: app/src/main/java/com/proyek/maganggsp/util/AppUtils.kt - COMPLETE UTILITY CONSOLIDATION
package com.proyek.maganggsp.util

import android.content.Context
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.isVisible
import com.facebook.shimmer.ShimmerFrameLayout
import com.proyek.maganggsp.R
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.util.exceptions.AppException
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * COMPLETE UTILITY CONSOLIDATION
 * Merges ALL scattered utility classes into single comprehensive utility:
 * - Formatters → Unified formatting methods
 * - ErrorDisplayHandler → Error handling methods
 * - EmptyStateHandler → Empty state management
 * - LoadingStateHandler → Loading state management
 * - ValidationUtils → Validation methods
 * Eliminates ALL other utility classes
 */
object AppUtils {

    // ========================================
    // FORMATTING UTILITIES (Consolidated from Formatters.kt)
    // ========================================

    /**
     * Format mata uang ke format Rupiah Indonesia
     */
    fun formatCurrency(amount: Long): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(amount)
    }

    /**
     * Format tanggal ke format Indonesia yang mudah dibaca
     */
    fun formatDate(dateString: String): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(dateString)

            val readableFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
            readableFormat.format(date!!)
        } catch (e: Exception) {
            dateString // Return original if parsing fails
        }
    }

    /**
     * Format PPID untuk display dengan truncation jika perlu
     */
    fun formatPpid(ppid: String): String {
        return when {
            ppid.length > 15 -> "${ppid.take(10)}...${ppid.takeLast(4)}"
            ppid.isBlank() -> "PPID tidak tersedia"
            else -> ppid
        }
    }

    /**
     * Format nomor telepon ke format Indonesia
     */
    fun formatPhoneNumber(phone: String): String {
        return when {
            phone.startsWith("+62") -> phone
            phone.startsWith("08") -> "+62${phone.substring(1)}"
            phone.startsWith("62") -> "+$phone"
            phone.isNotBlank() -> phone
            else -> "No. HP tidak tersedia"
        }
    }

    // ========================================
    // ERROR HANDLING UTILITIES (Consolidated from ErrorDisplayHandler.kt)
    // ========================================

    /**
     * Show error dengan mapping ke bahasa Indonesia
     */
    fun showError(context: Context, error: AppException) {
        val message = mapExceptionToIndonesianMessage(error)
        Toast.makeText(context, message, Toast.LENGTH_LONG).show()
    }

    /**
     * Show simple error message
     */
    fun showError(context: Context, message: String) {
        Toast.makeText(context, "❌ $message", Toast.LENGTH_SHORT).show()
    }

    /**
     * Show success message
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
    // UNIFIED LOADING STATE MANAGEMENT (Consolidated from LoadingStateHandler.kt)
    // ========================================

    /**
     * UNIFIED: Handle loading state untuk standard shimmer + content pattern
     */
    fun <T> handleLoadingState(
        resource: Resource<T>,
        shimmerView: ShimmerFrameLayout,
        contentView: View,
        emptyView: View? = null
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

    /**
     * UNIFIED: Handle dual loading (card + mutations shimmer pattern)
     */
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
                emptyView?.isVisible = true
            }
            is Resource.Empty -> {
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

    private fun evaluateDataAvailability(data: Any?): Boolean {
        return when (data) {
            is List<*> -> data.isNotEmpty()
            is String -> data.isNotBlank()
            null -> false
            else -> true
        }
    }

    // ========================================
    // EMPTY STATE MANAGEMENT (Consolidated from EmptyStateHandler.kt)
    // ========================================

    /**
     * UNIFIED: Apply contextual empty state messages dalam bahasa Indonesia
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
                if (searchQuery.length < 5) "Ketik minimal 5 karakter PPID untuk pencarian"
                else "Tidak ada hasil untuk PPID \"$searchQuery\""
            }
            context == "home" && !isSearchMode && itemCount == 0 ->
                "Belum ada receipt tersedia.\nTarik ke bawah untuk refresh."
            context == "transactions" && itemCount == 0 ->
                "Belum ada log transaksi.\nData akan muncul jika ada aktivitas."
            context == "detail" && itemCount == 0 ->
                "Informasi tidak tersedia untuk PPID ini."
            context == "search" && itemCount == 0 ->
                "Tidak ditemukan data untuk pencarian \"$searchQuery\""
            else -> "Tidak ada data tersedia"
        }

        textView.text = message
        textView.isVisible = itemCount == 0
        textView.textAlignment = View.TEXT_ALIGNMENT_CENTER
    }

    // ========================================
    // VALIDATION UTILITIES (Consolidated from ValidationUtils.kt)
    // ========================================

    /**
     * Validate PPID format
     */
    fun isValidPpid(ppid: String): Boolean {
        return ppid.isNotBlank() && ppid.length >= 5
    }

    /**
     * Validate email format
     */
    fun isValidEmail(email: String): Boolean {
        return android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
    }

    /**
     * Enhanced PPID validation with format checking
     */
    fun validatePpidFormat(ppid: String): ValidationResult {
        return when {
            ppid.isBlank() -> ValidationResult(false, "PPID tidak boleh kosong")
            ppid.length < 5 -> ValidationResult(false, "PPID minimal 5 karakter")
            ppid.length < 8 -> ValidationResult(false, "PPID terlalu pendek")
            !ppid.matches("^[A-Z]{3,}[0-9]+.*$".toRegex()) -> {
                ValidationResult(false, "Format PPID tidak valid. Gunakan format PIDLKTD0025")
            }
            else -> ValidationResult(true, "Valid")
        }
    }

    data class ValidationResult(val isValid: Boolean, val message: String)

    // ========================================
    // PLACEHOLDER DATA UTILITIES (For development)
    // ========================================

    /**
     * Create placeholder receipt untuk testing
     */
    fun createPlaceholderReceipt(ppid: String): Receipt {
        return Receipt(
            refNumber = "REF-${System.currentTimeMillis().toString().takeLast(6)}",
            idPelanggan = ppid,
            amount = (50000..500000L).random(),
            logged = "2024-01-15T10:30:00.000Z",
            ppid = ppid,
            namaLoket = "Loket Testing $ppid",
            nomorHP = "+6281234567${(10..99).random()}",
            email = "test@loket$ppid.com"
        )
    }

    /**
     * Create placeholder transaction logs untuk testing
     */
    fun createPlaceholderTransactionLogs(ppid: String, count: Int = 5): List<TransactionLog> {
        val transactions = mutableListOf<TransactionLog>()
        var balance = 1000000L

        repeat(count) { index ->
            val amount = if (index % 3 == 0) -(10000..50000L).random() else (25000..100000L).random()
            balance += amount

            transactions.add(
                TransactionLog(
                    tldRefnum = "TXN${String.format("%03d", index + 1)}-TEST",
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
    // NAVIGATION UTILITIES
    // ========================================

    /**
     * Safe navigation helper
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
     * Consistent logging dengan tag
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

    /**
     * Get comprehensive debug info
     */
    fun getDebugInfo(context: Context): String {
        return """
        📱 REFACTORED GESPAY ADMIN DEBUG INFO:
        - Context: ${context.javaClass.simpleName}
        - Time: ${formatDate(SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'").format(Date()))}
        - Memory: ${Runtime.getRuntime().freeMemory() / 1024 / 1024} MB free
        - Version: ${com.proyek.maganggsp.BuildConfig.VERSION_NAME}
        - Build: ${com.proyek.maganggsp.BuildConfig.BUILD_TYPE}
        - API URL: ${com.proyek.maganggsp.BuildConfig.BASE_URL}
        - Data Focus: Receipt/TransactionLog (COMPLETE REFACTOR)
        - Architecture: Unified API Integration
        - Utilities: CONSOLIDATED (All-in-One)
        - Feature Flags: ELIMINATED
        """.trimIndent()
    }
}
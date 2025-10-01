// File: app/src/main/java/com/proyek/maganggsp/util/AppUtils.kt - BRIDGE PATTERN
package com.proyek.maganggsp.util

import android.content.Context
import android.view.View
import android.widget.TextView
import com.facebook.shimmer.ShimmerFrameLayout
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.util.exceptions.AppException

/**
 * BRIDGE PATTERN: Backward compatibility layer
 * Delegates semua calls ke modular utilities untuk quick fix
 * TODO: Migrate gradually ke modular utilities langsung
 */
object AppUtils {

    // ============================================
    // LOGGING - Delegate ke LoggingUtils
    // ============================================

    fun logDebug(tag: String, message: String) {
        LoggingUtils.logDebug(tag, message)
    }

    fun logInfo(tag: String, message: String) {
        LoggingUtils.logInfo(tag, message)
    }

    fun logWarning(tag: String, message: String) {
        LoggingUtils.logWarning(tag, message)
    }

    fun logError(tag: String, message: String, exception: Throwable? = null) {
        LoggingUtils.logError(tag, message, exception)
    }

    fun logError(tag: String, message: String, exception: AppException) {
        LoggingUtils.logError(tag, message, exception)
    }

    // ============================================
    // ERROR HANDLING - Delegate ke ErrorHandler
    // ============================================

    fun showError(context: Context, exception: AppException) {
        ErrorHandler.showError(context, exception)
    }

    fun showError(context: Context, message: String) {
        ErrorHandler.showError(context, message)
    }

    fun showSuccess(context: Context, message: String) {
        ErrorHandler.showSuccess(context, message)
    }

    // ============================================
    // FORMATTING - Delegate ke Formatters
    // ============================================

    fun formatCurrency(amount: Long): String {
        return Formatters.formatCurrency(amount)
    }

    fun formatCurrencyWithSign(amount: Long): String {
        return Formatters.formatCurrencyWithSign(amount)
    }

    fun formatDate(dateString: String): String {
        return Formatters.formatDate(dateString)
    }

    fun formatDateShort(dateString: String): String {
        return Formatters.formatDateShort(dateString)
    }

    fun formatPhoneNumber(phone: String): String {
        return Formatters.formatPhoneNumber(phone)
    }

    fun formatPpid(ppid: String): String {
        return Formatters.formatPpid(ppid)
    }

    fun formatPpidForCard(ppid: String): String {
        return Formatters.formatPpidForCard(ppid)
    }

    // ============================================
    // VALIDATION - Delegate ke ValidationUtils
    // ============================================

    fun isValidPpid(ppid: String): Boolean {
        return ValidationUtils.isValidPpid(ppid)
    }

    fun isValidEmail(email: String): Boolean {
        return ValidationUtils.isValidEmail(email)
    }

    fun isValidPhoneNumber(phone: String): Boolean {
        return ValidationUtils.isValidPhoneNumber(phone)
    }

    // ============================================
    // LOADING STATE - Delegate ke UiStateHelper
    // ============================================

    fun <T> handleLoadingState(
        resource: Resource<T>,
        shimmerView: View,
        contentView: View,
        emptyView: View? = null
    ) {
        UiStateHelper.handleLoadingState(resource, shimmerView, contentView, emptyView)
    }

    fun <T> handleDualLoadingState(
        resource: Resource<T>,
        primaryShimmer: ShimmerFrameLayout,
        primaryContent: View,
        secondaryShimmer: ShimmerFrameLayout? = null,
        secondaryContent: View? = null,
        emptyView: View? = null
    ) {
        UiStateHelper.handleDualLoadingState(
            resource, primaryShimmer, primaryContent,
            secondaryShimmer, secondaryContent, emptyView
        )
    }

    fun applyEmptyState(textView: TextView, context: String, itemCount: Int) {
        UiStateHelper.applyEmptyState(textView, context, itemCount)
    }

    // ============================================
    // PLACEHOLDER DATA - Delegate ke PlaceholderDataGenerator
    // ============================================

    fun createPlaceholderReceipt(ppid: String): Receipt {
        return PlaceholderDataGenerator.createPlaceholderReceipt(ppid)
    }

    fun createPlaceholderTransactionLogs(ppid: String, count: Int = 5): List<TransactionLog> {
        return PlaceholderDataGenerator.createPlaceholderTransactionLogs(ppid, count)
    }

    fun createPlaceholderReceipts(count: Int = 5): List<Receipt> {
        return PlaceholderDataGenerator.createPlaceholderReceipts(count)
    }
}
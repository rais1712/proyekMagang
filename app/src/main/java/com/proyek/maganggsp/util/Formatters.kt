// File: app/src/main/java/com/proyek/maganggsp/util/Formatters.kt
package com.proyek.maganggsp.util

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * MODULAR: Formatting utilities
 * Extracted from unified AppUtils.kt untuk better modularity
 */
object Formatters {

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
     * Format currency with proper sign for transaction display
     * +Rp50.000 or -Rp100.000 (based on UI mockup)
     */
    fun formatCurrencyWithSign(amount: Long): String {
        val formatted = formatCurrency(kotlin.math.abs(amount))
        return if (amount >= 0) "+$formatted" else "-$formatted"
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
     * Format date to short format (for UI cards): 15 Juli 2025
     */
    fun formatDateShort(dateString: String): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(dateString)

            val shortFormat = SimpleDateFormat("dd MMMM yyyy", Locale("in", "ID"))
            shortFormat.format(date!!)
        } catch (e: Exception) {
            dateString
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
     * Format PPID for card display: #101010 (based on UI mockup)
     */
    fun formatPpidForCard(ppid: String): String {
        return "#$ppid"
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

    /**
     * Format balance info for UI: Saldo: Rp29.850.000
     */
    fun formatBalanceInfo(balance: Long): String {
        return "Saldo: ${formatCurrency(balance)}"
    }

    /**
     * Format transaction description for display
     */
    fun formatTransactionDescription(refNumber: String): String {
        return "No. Ref: $refNumber"
    }
}
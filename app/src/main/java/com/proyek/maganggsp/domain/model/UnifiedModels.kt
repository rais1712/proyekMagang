// File: app/src/main/java/com/proyek/maganggsp/domain/model/UnifiedModels.kt - SINGLE SOURCE OF TRUTH
package com.proyek.maganggsp.domain.model

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * UNIFIED: Single Loket model untuk semua use cases
 * Menggabungkan semua requirements dari profile, search, dan management
 */
data class Loket(
    val ppid: String,
    val namaLoket: String,
    val nomorHP: String,
    val alamat: String,
    val email: String,
    val status: LoketStatus,
    val saldoTerakhir: Long = 0L,
    val tanggalAkses: String = "",
    val receipts: List<Receipt> = emptyList()
) {

    // UTILITY: Format saldo ke Rupiah
    fun getFormattedSaldo(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(saldoTerakhir)
    }

    // UTILITY: Display title untuk UI
    fun getDisplayTitle(): String = "$namaLoket ($ppid)"

    // UTILITY: Format nomor HP
    fun getFormattedPhone(): String {
        return when {
            nomorHP.startsWith("+62") -> nomorHP
            nomorHP.startsWith("08") -> "+62${nomorHP.substring(1)}"
            nomorHP.startsWith("62") -> "+$nomorHP"
            else -> nomorHP
        }
    }

    // VALIDATION: Check if data is valid
    fun hasValidData(): Boolean = ppid.isNotBlank() && namaLoket.isNotBlank()

    // STATUS: Display text
    fun getStatusDisplayText(): String = when (status) {
        LoketStatus.NORMAL -> "Normal"
        LoketStatus.BLOCKED -> "Diblokir"
        LoketStatus.FLAGGED -> "Ditandai"
    }

    // STATUS: Check methods
    fun isBlocked(): Boolean = status == LoketStatus.BLOCKED
    fun isFlagged(): Boolean = status == LoketStatus.FLAGGED
    fun isNormal(): Boolean = status == LoketStatus.NORMAL

    // API LOGIC: Get original PPID (without "blok" suffix)
    fun getOriginalPpid(): String = ppid.removeSuffix("blok")

    // API LOGIC: Get blocked PPID (with "blok" suffix)
    fun getBlockedPpid(): String = if (ppid.endsWith("blok")) ppid else "${ppid}blok"

    // SEARCH: For search functionality
    fun matchesSearchQuery(query: String): Boolean {
        val lowerQuery = query.lowercase()
        return namaLoket.lowercase().contains(lowerQuery) ||
                nomorHP.contains(lowerQuery) ||
                ppid.lowercase().contains(lowerQuery) ||
                email.lowercase().contains(lowerQuery)
    }
}

/**
 * UNIFIED: LoketStatus enum dengan proper mapping
 */
enum class LoketStatus {
    NORMAL,    // Default status
    BLOCKED,   // Diblokir (ppid ends with "blok")
    FLAGGED;   // Ditandai untuk monitoring (future use)

    companion object {
        /**
         * REAL API LOGIC: Determine status from PPID
         * Block/unblock logic berdasarkan suffix "blok" di PPID
         */
        fun fromPpid(ppid: String?): LoketStatus {
            return if (ppid?.endsWith("blok") == true) {
                BLOCKED
            } else {
                NORMAL
            }
        }

        fun fromString(status: String?): LoketStatus {
            return when (status?.uppercase()) {
                "BLOCKED", "DIBLOKIR" -> BLOCKED
                "FLAGGED", "DITANDAI" -> FLAGGED
                else -> NORMAL
            }
        }
    }
}

/**
 * UNIFIED: Receipt model untuk transaction display
 * Single model untuk semua receipt use cases
 */
data class Receipt(
    val refNumber: String,
    val idPelanggan: String,
    val tanggal: String,
    val mutasi: Long,
    val totalSaldo: Long,
    val ppid: String,
    val tipeTransaksi: String = "Receipt"
) {

    // BACKWARD COMPATIBILITY: Legacy properties
    val amount: Long get() = mutasi
    val logged: String get() = tanggal

    // FORMATTING: Amount with +/- sign
    fun getFormattedAmount(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return if (mutasi >= 0) {
            "+${numberFormat.format(mutasi)}"
        } else {
            numberFormat.format(mutasi)
        }
    }

    // FORMATTING: Balance
    fun getFormattedSaldo(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(totalSaldo)
    }

    // FORMATTING: Date
    fun getFormattedDate(): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(tanggal)

            val readableFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
            readableFormat.format(date!!)
        } catch (e: Exception) {
            tanggal // Return original if parsing fails
        }
    }

    // TRANSACTION TYPE
    fun isIncomingTransaction(): Boolean = mutasi >= 0
    fun isOutgoingTransaction(): Boolean = mutasi < 0

    // DISPLAY: Description
    fun getDisplayDescription(): String = "Ref: $refNumber | ID: $idPelanggan"
    fun getSaldoDisplayText(): String = "Saldo: ${getFormattedSaldo()}"

    // VALIDATION
    fun hasValidData(): Boolean = refNumber.isNotBlank() && idPelanggan.isNotBlank() && ppid.isNotBlank()
}

/**
 * UNIFIED: TransactionLog model for detailed transaction view
 */
data class TransactionLog(
    val tldRefnum: String,
    val tldPan: String,
    val tldIdpel: String,
    val tldAmount: Long,
    val tldBalance: Long,
    val tldDate: String,
    val tldPpid: String
) {

    // FORMATTING: Amount with proper signs
    fun getFormattedAmount(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return if (tldAmount >= 0) {
            "+${numberFormat.format(tldAmount)}"
        } else {
            numberFormat.format(tldAmount)
        }
    }

    // FORMATTING: Balance
    fun getFormattedBalance(): String {
        val localeID = Locale("in", "ID")
        val numberFormat = NumberFormat.getCurrencyInstance(localeID)
        numberFormat.maximumFractionDigits = 0
        return numberFormat.format(tldBalance)
    }

    // FORMATTING: Date
    fun getFormattedDate(): String {
        return try {
            val isoFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            isoFormat.timeZone = TimeZone.getTimeZone("UTC")
            val date = isoFormat.parse(tldDate)

            val readableFormat = SimpleDateFormat("dd MMMM yyyy, HH:mm", Locale("in", "ID"))
            readableFormat.format(date!!)
        } catch (e: Exception) {
            tldDate // Return original if parsing fails
        }
    }

    // TRANSACTION TYPE
    fun isIncomingTransaction(): Boolean = tldAmount >= 0
    fun isOutgoingTransaction(): Boolean = tldAmount < 0

    fun getTransactionType(): TransactionType = if (isIncomingTransaction()) {
        TransactionType.INCOMING
    } else {
        TransactionType.OUTGOING
    }

    // DISPLAY
    fun getDisplayDescription(): String = "Ref: $tldRefnum | ID: $tldIdpel"
    fun getBalanceDisplayText(): String = "Saldo: ${getFormattedBalance()}"

    // VALIDATION
    fun hasValidData(): Boolean = tldRefnum.isNotBlank() && tldIdpel.isNotBlank() && tldPpid.isNotBlank()

    enum class TransactionType {
        INCOMING, OUTGOING
    }
}

/**
 * UNIFIED: LoketSearchHistory for local search history management
 */
data class LoketSearchHistory(
    val ppid: String,
    val namaLoket: String,
    val nomorHP: String,
    val email: String? = null,
    val alamat: String? = null,
    val status: LoketStatus = LoketStatus.NORMAL,
    val tanggalAkses: Long = System.currentTimeMillis(),
    val jumlahAkses: Int = 1
) {

    // FORMATTING: Access date
    fun getFormattedTanggalAkses(): String {
        val date = Date(tanggalAkses)
        val format = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("in", "ID"))
        return format.format(date)
    }

    // DISPLAY: History text
    fun getDisplayText(): String = "$namaLoket - Diakses ${jumlahAkses}x"

    // CONVERSION: To Loket object
    fun toLoket(): Loket {
        return Loket(
            ppid = ppid,
            namaLoket = namaLoket,
            nomorHP = nomorHP,
            alamat = alamat ?: "",
            email = email ?: "",
            status = status,
            tanggalAkses = getFormattedTanggalAkses()
        )
    }
}

/**
 * EXTENSION FUNCTIONS: For collections
 */

// Receipt extensions
fun List<Receipt>.getTotalAmount(): Long = sumOf { it.mutasi }
fun List<Receipt>.getValidReceipts(): List<Receipt> = filter { it.hasValidData() }
fun List<Receipt>.sortByAmountDescending(): List<Receipt> = sortedByDescending { it.mutasi }
fun List<Receipt>.sortByDate(): List<Receipt> = sortedBy { it.tanggal }

// TransactionLog extensions
fun List<TransactionLog>.getTotalIncoming(): Long =
    filter { it.isIncomingTransaction() }.sumOf { it.tldAmount }

fun List<TransactionLog>.getTotalOutgoing(): Long =
    filter { it.isOutgoingTransaction() }.sumOf { kotlin.math.abs(it.tldAmount) }

fun List<TransactionLog>.getNetAmount(): Long = sumOf { it.tldAmount }
fun List<TransactionLog>.getLatestBalance(): Long = firstOrNull()?.tldBalance ?: 0L

fun List<TransactionLog>.sortByDateDescending(): List<TransactionLog> =
    sortedByDescending { it.tldDate }

// Loket extensions
fun List<Loket>.getBlockedLokets(): List<Loket> = filter { it.isBlocked() }
fun List<Loket>.getFlaggedLokets(): List<Loket> = filter { it.isFlagged() }
fun List<Loket>.getNormalLokets(): List<Loket> = filter { it.isNormal() }

fun List<Loket>.searchByQuery(query: String): List<Loket> =
    filter { it.matchesSearchQuery(query) }

/**
 * VALIDATION UTILITIES
 */
object ModelValidation {

    fun isValidPpid(ppid: String?): Boolean =
        !ppid.isNullOrBlank() && ppid.length >= 5

    fun isValidPhoneNumber(phone: String?): Boolean =
        !phone.isNullOrBlank() && phone.length >= 10

    fun isValidEmail(email: String?): Boolean =
        !email.isNullOrBlank() && email.contains("@")

    fun formatPhoneNumber(phone: String): String {
        val cleaned = phone.replace(Regex("[^0-9+]"), "")
        return when {
            cleaned.startsWith("+62") -> cleaned
            cleaned.startsWith("62") -> "+$cleaned"
            cleaned.startsWith("08") -> "+62${cleaned.substring(1)}"
            else -> cleaned
        }
    }
}
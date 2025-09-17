// ============================================================================
// File: app/src/main/java/com/proyek/maganggsp/data/dto/ProfileApiDtos.kt
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.model.LoketStatus

/**
 * 🎯 PHASE 1: API DTO MODELS
 * Reasonable assumptions based on existing domain models and API structure
 */

// ============================================================================
// PROFILE API DTOs
// ============================================================================

/**
 * ProfileResponse: Response from GET /profiles/ppid/{ppid}
 * Maps to Receipt domain model
 */
data class ProfileResponse(
    @SerializedName("ppid")
    val ppid: String?,

    @SerializedName("namaLoket")
    val namaLoket: String?,

    @SerializedName("nomorHP")
    val nomorHP: String?,

    @SerializedName("alamat")
    val alamat: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("saldoTerakhir")
    val saldoTerakhir: Long?,

    @SerializedName("tanggalAkses")
    val tanggalAkses: String?,

    // Additional fields that might come from API
    @SerializedName("status")
    val status: String?,

    @SerializedName("refNumber")
    val refNumber: String?,

    @SerializedName("idPelanggan")
    val idPelanggan: String?
)

/**
 * TransactionResponse: Response from GET /trx/ppid/{ppid}
 * Maps directly to TransactionLog domain model
 */
data class TransactionResponse(
    @SerializedName("tldRefnum")
    val tldRefnum: String?,

    @SerializedName("tldPan")
    val tldPan: String?,

    @SerializedName("tldIdpel")
    val tldIdpel: String?,

    @SerializedName("tldAmount")
    val tldAmount: Long?,

    @SerializedName("tldBalance")
    val tldBalance: Long?,

    @SerializedName("tldDate")
    val tldDate: String?,

    @SerializedName("tldPpid")
    val tldPpid: String?
)

/**
 * UpdateProfileRequest: Request body for PUT /profiles/ppid/{ppid}
 * Based on actual API request: {"mpPpid": "PIDLKTD0025blok"}
 */
data class UpdateProfileRequest(
    @SerializedName("mpPpid")
    val mpPpid: String
)

// ============================================================================
// DTO TO DOMAIN MAPPING EXTENSIONS
// ============================================================================

/**
 * ProfileResponse to Receipt mapping
 */
fun ProfileResponse.toReceipt(): Receipt {
    return Receipt(
        refNumber = refNumber ?: "REF-${ppid ?: "UNKNOWN"}",
        idPelanggan = idPelanggan ?: ppid ?: "",
        amount = saldoTerakhir ?: 0L,
        logged = tanggalAkses ?: getCurrentTimestamp(),
        ppid = ppid ?: "",
        namaLoket = namaLoket ?: "",
        nomorHP = nomorHP ?: "",
        email = email ?: "",
        alamat = alamat ?: "",
        status = LoketStatus.fromPpid(ppid)
    )
}

/**
 * TransactionResponse to TransactionLog mapping
 */
fun TransactionResponse.toTransactionLog(): TransactionLog {
    return TransactionLog(
        tldRefnum = tldRefnum ?: "",
        tldPan = tldPan ?: "",
        tldIdpel = tldIdpel ?: "",
        tldAmount = tldAmount ?: 0L,
        tldBalance = tldBalance ?: 0L,
        tldDate = tldDate ?: getCurrentTimestamp(),
        tldPpid = tldPpid ?: ""
    )
}

/**
 * Collection mapping extensions
 */
fun List<TransactionResponse>.toTransactionLogs(): List<TransactionLog> =
    mapNotNull { response ->
        if (response.tldRefnum?.isNotBlank() == true) {
            response.toTransactionLog()
        } else {
            null // Skip invalid entries
        }
    }

// ============================================================================
// BLOCK/UNBLOCK REQUEST HELPERS
// ============================================================================

/**
 * Create block request
 * Converts: "PIDLKTD0025" → {"mpPpid": "PIDLKTD0025blok"}
 */
fun createBlockRequest(originalPpid: String): UpdateProfileRequest {
    val blockedPpid = if (originalPpid.endsWith("blok")) {
        originalPpid // Already blocked
    } else {
        "${originalPpid}blok"
    }
    return UpdateProfileRequest(mpPpid = blockedPpid)
}

/**
 * Create unblock request
 * Converts: "PIDLKTD0025blok" → {"mpPpid": "PIDLKTD0025"}
 */
fun createUnblockRequest(blockedPpid: String): UpdateProfileRequest {
    val originalPpid = if (blockedPpid.endsWith("blok")) {
        blockedPpid.removeSuffix("blok")
    } else {
        blockedPpid // Not blocked
    }
    return UpdateProfileRequest(mpPpid = originalPpid)
}

// ============================================================================
// UTILITY FUNCTIONS
// ============================================================================

/**
 * Get current timestamp in ISO format
 */
private fun getCurrentTimestamp(): String {
    val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
    formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
    return formatter.format(java.util.Date())
}

/**
 * Safe string extraction with default values
 */
fun String?.orDefault(default: String): String = if (isNullOrBlank()) default else this

/**
 * Safe long extraction with default values
 */
fun Long?.orDefault(default: Long = 0L): Long = this ?: default

// ============================================================================
// PLACEHOLDER DATA CREATORS (for development/testing)
// ============================================================================

/**
 * Create placeholder ProfileResponse for development
 */
fun createPlaceholderProfileResponse(ppid: String): ProfileResponse {
    return ProfileResponse(
        ppid = ppid,
        namaLoket = "Loket Test $ppid",
        nomorHP = "+6281234567890",
        alamat = "Jl. Test No. 123, Bandung",
        email = "test@loket${ppid}.com",
        saldoTerakhir = (100000..1000000L).random(),
        tanggalAkses = getCurrentTimestamp(),
        status = if (ppid.endsWith("blok")) "BLOCKED" else "NORMAL",
        refNumber = "REF-${System.currentTimeMillis()}",
        idPelanggan = ppid
    )
}

/**
 * Create placeholder TransactionResponse list for development
 */
fun createPlaceholderTransactionResponses(ppid: String, count: Int = 5): List<TransactionResponse> {
    val transactions = mutableListOf<TransactionResponse>()
    var balance = 1000000L

    repeat(count) { index ->
        val amount = if (index % 3 == 0) {
            -(10000..50000L).random() // Outgoing
        } else {
            (25000..100000L).random() // Incoming
        }
        balance += amount

        transactions.add(
            TransactionResponse(
                tldRefnum = "TXN${String.format("%03d", index + 1)}-${ppid}",
                tldPan = "1234****5678",
                tldIdpel = ppid,
                tldAmount = amount,
                tldBalance = balance,
                tldDate = getCurrentTimestamp(),
                tldPpid = ppid
            )
        )
    }

    return transactions.reversed() // Most recent first
}

// ============================================================================
// VALIDATION HELPERS
// ============================================================================

/**
 * Validate ProfileResponse data
 */
fun ProfileResponse.isValid(): Boolean {
    return !ppid.isNullOrBlank() && !namaLoket.isNullOrBlank()
}

/**
 * Validate TransactionResponse data
 */
fun TransactionResponse.isValid(): Boolean {
    return !tldRefnum.isNullOrBlank() && !tldPpid.isNullOrBlank()
}

/**
 * Filter valid responses from API
 */
fun List<TransactionResponse>.filterValid(): List<TransactionResponse> =
    filter { it.isValid() }

// ============================================================================
// DEBUG HELPERS
// ============================================================================

/**
 * Create debug info string for ProfileResponse
 */
fun ProfileResponse.toDebugString(): String {
    return """
    ProfileResponse Debug:
    - PPID: $ppid
    - Nama: $namaLoket  
    - Status: ${LoketStatus.fromPpid(ppid)}
    - Saldo: $saldoTerakhir
    - Valid: ${isValid()}
    """.trimIndent()
}

/**
 * Create debug info string for TransactionResponse list
 */
fun List<TransactionResponse>.toDebugString(): String {
    val totalAmount = sumOf { it.tldAmount ?: 0L }
    val validCount = count { it.isValid() }

    return """
    TransactionResponse List Debug:
    - Total Count: $size
    - Valid Count: $validCount
    - Total Amount: $totalAmount
    - Latest Balance: ${firstOrNull()?.tldBalance ?: 0L}
    """.trimIndent()
}
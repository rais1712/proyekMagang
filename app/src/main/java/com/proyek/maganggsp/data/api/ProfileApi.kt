// File: app/src/main/java/com/proyek/maganggsp/data/api/ProfileApi.kt - REAL ENDPOINT ALIGNED
package com.proyek.maganggsp.data.api

import retrofit2.Response
import retrofit2.http.*

/**
 * UNIFIED: Single ProfileApi yang align dengan real backend endpoints
 * Eliminates confusion antara LoketApi, ProfileApi, dan endpoint yang tidak ada
 */
interface ProfileApi {

    /**
     * REAL ENDPOINT: Get profile data dengan receipts included
     * URL: GET /api/profiles/ppid/{ppid}
     * Headers: Authorization: Bearer {token}, Content-Type: application/json
     * Response: Profile data + embedded receipts list
     */
    @GET("profiles/ppid/{ppid}")
    suspend fun getProfile(@Path("ppid") ppid: String): ProfileResponse

    /**
     * REAL ENDPOINT: Get transaction logs for specific PPID
     * URL: GET /api/trx/ppid/{ppid}
     * Headers: Authorization: Bearer {token}, Content-Type: application/json
     * Response: List<TransactionResponse>
     */
    @GET("trx/ppid/{ppid}")
    suspend fun getTransactions(@Path("ppid") ppid: String): List<TransactionResponse>

    /**
     * REAL ENDPOINT: Update profile (used for block/unblock)
     * URL: PUT /api/profiles/ppid/{ppid}
     * Headers: Authorization: Bearer {token}, Content-Type: application/json
     * Body Examples:
     * - Block: {"mpPpid": "PIDLKTD0025blok"}
     * - Unblock: {"mpPpid": "PIDLKTD0025"}
     */
    @PUT("profiles/ppid/{ppid}")
    suspend fun updateProfile(
        @Path("ppid") ppid: String,
        @Body request: UpdateProfileRequest
    ): Response<Unit>
}

/**
 * UNIFIED DTO STRUCTURES: Based on actual API responses
 */

/**
 * Profile Response Structure - REAL API FORMAT
 */
data class ProfileResponse(
    val ppid: String?,
    val namaLoket: String?,
    val nomorHP: String?,
    val alamat: String?,
    val email: String?,
    val saldoTerakhir: Long?,
    val tanggalAkses: String?,
    val receipts: List<ReceiptResponse>? = emptyList() // Embedded receipts
)

/**
 * Receipt Response Structure - REAL API FORMAT
 */
data class ReceiptResponse(
    val refNumber: String?,
    val idPelanggan: String?,
    val tanggal: String?,
    val mutasi: Long?,
    val totalSaldo: Long?,
    val ppid: String?
)

/**
 * Transaction Response Structure - REAL API FORMAT
 */
data class TransactionResponse(
    val tldRefnum: String?,
    val tldPan: String?,
    val tldIdpel: String?,
    val tldAmount: Long?,
    val tldBalance: Long?,
    val tldDate: String?,
    val tldPpid: String?
)

/**
 * Update Profile Request - REAL API FORMAT
 * Used for block/unblock operations
 */
data class UpdateProfileRequest(
    val mpPpid: String // The new PPID value (with or without "blok" suffix)
)

/**
 * DOMAIN MAPPING EXTENSIONS - Clean dan Unified
 */

// Profile to Loket mapping (for DetailLoket)
fun ProfileResponse.toLoketDomain(): com.proyek.maganggsp.domain.model.Loket {
    return com.proyek.maganggsp.domain.model.Loket(
        ppid = this.ppid ?: "",
        namaLoket = this.namaLoket ?: "Unknown Loket",
        nomorHP = this.nomorHP ?: "",
        alamat = this.alamat ?: "",
        email = this.email ?: "",
        status = determineLoketStatus(this.ppid),
        saldoTerakhir = this.saldoTerakhir ?: 0L,
        tanggalAkses = this.tanggalAkses ?: "",
        receipts = this.receipts?.mapNotNull { it.toDomain() } ?: emptyList()
    )
}

// Profile to Receipt mapping (for TransactionLogViewModel compatibility)
fun ProfileResponse.toReceiptDomain(): com.proyek.maganggsp.domain.model.Receipt {
    return com.proyek.maganggsp.domain.model.Receipt(
        refNumber = "PROFILE-${this.ppid ?: "UNKNOWN"}",
        idPelanggan = this.ppid ?: "",
        tanggal = this.tanggalAkses ?: "",
        mutasi = this.saldoTerakhir ?: 0L,
        totalSaldo = this.saldoTerakhir ?: 0L,
        ppid = this.ppid ?: "",
        tipeTransaksi = "Profile Info"
    )
}

// Receipt response to domain
fun ReceiptResponse.toDomain(): com.proyek.maganggsp.domain.model.Receipt? {
    if (refNumber.isNullOrBlank() || ppid.isNullOrBlank()) return null

    return com.proyek.maganggsp.domain.model.Receipt(
        refNumber = refNumber,
        idPelanggan = idPelanggan ?: "",
        tanggal = tanggal ?: "",
        mutasi = mutasi ?: 0L,
        totalSaldo = totalSaldo ?: 0L,
        ppid = ppid
    )
}

// Transaction response to domain
fun TransactionResponse.toDomain(): com.proyek.maganggsp.domain.model.TransactionLog {
    return com.proyek.maganggsp.domain.model.TransactionLog(
        tldRefnum = tldRefnum ?: "",
        tldPan = tldPan ?: "",
        tldIdpel = tldIdpel ?: "",
        tldAmount = tldAmount ?: 0L,
        tldBalance = tldBalance ?: 0L,
        tldDate = tldDate ?: "",
        tldPpid = tldPpid ?: ""
    )
}

/**
 * BLOCK/UNBLOCK HELPERS
 */

// Determine loket status dari PPID
private fun determineLoketStatus(ppid: String?): com.proyek.maganggsp.domain.model.LoketStatus {
    return if (ppid?.endsWith("blok") == true) {
        com.proyek.maganggsp.domain.model.LoketStatus.BLOCKED
    } else {
        com.proyek.maganggsp.domain.model.LoketStatus.NORMAL
    }
}

// Create block request
fun createBlockRequest(originalPpid: String): UpdateProfileRequest {
    val blockedPpid = if (originalPpid.endsWith("blok")) {
        originalPpid // Already blocked
    } else {
        "${originalPpid}blok"
    }
    return UpdateProfileRequest(mpPpid = blockedPpid)
}

// Create unblock request
fun createUnblockRequest(blockedPpid: String): UpdateProfileRequest {
    val originalPpid = if (blockedPpid.endsWith("blok")) {
        blockedPpid.removeSuffix("blok")
    } else {
        blockedPpid // Not blocked
    }
    return UpdateProfileRequest(mpPpid = originalPpid)
}
// File: app/src/main/java/com/proyek/maganggsp/data/api/LoketApi.kt - REAL API IMPLEMENTATION
package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.LoketProfileResponse
import com.proyek.maganggsp.data.dto.TransactionResponse
import com.proyek.maganggsp.data.dto.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * REAL API: LoketApi berdasarkan endpoint yang benar-benar ada
 * Menggunakan ProfileApi sebagai base dengan endpoint yang sudah terbukti working
 */
interface LoketApi {

    /**
     * REAL ENDPOINT: Get loket profile with receipts
     * Maps to: GET /profiles/ppid/{ppid}
     */
    @GET("profiles/ppid/{ppid}")
    suspend fun getLoketProfile(@Path("ppid") ppid: String): LoketProfileResponse

    /**
     * REAL ENDPOINT: Get transaction logs for specific loket
     * Maps to: GET /trx/ppid/{ppid}
     */
    @GET("trx/ppid/{ppid}")
    suspend fun getLoketTransactions(@Path("ppid") ppid: String): List<TransactionResponse>

    /**
     * REAL ENDPOINT: Update loket profile (including block/unblock)
     * Maps to: PUT /profiles/ppid/{ppid}
     *
     * Block/Unblock Logic:
     * - Block: {"mpPpid": "PIDLKTD0025blok"}
     * - Unblock: {"mpPpid": "PIDLKTD0025"}
     */
    @PUT("profiles/ppid/{ppid}")
    suspend fun updateLoketProfile(
        @Path("ppid") ppid: String,
        @Body request: UpdateProfileRequest
    ): Response<Unit>

    /**
     * CONVENIENCE METHODS: Block/Unblock using updateLoketProfile
     * These are helper methods that will be implemented in repository layer
     */
    // blockLoket() -> calls updateLoketProfile() with "ppid + blok"
    // unblockLoket() -> calls updateLoketProfile() with original ppid
}

/**
 * REAL DTO: Based on actual API response structure
 */
data class LoketProfileResponse(
    val ppid: String?,
    val namaLoket: String?,
    val nomorHP: String?,
    val alamat: String?,
    val email: String?,
    val saldoTerakhir: Long?,
    val tanggalAkses: String?,
    val receipts: List<ReceiptResponse>? = emptyList()
)

data class ReceiptResponse(
    val refNumber: String?,
    val idPelanggan: String?,
    val tanggal: String?,
    val mutasi: Long?,
    val totalSaldo: Long?,
    val ppid: String?
)

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
 * REAL UPDATE REQUEST: Based on actual API requirement
 * Body: {"mpPpid": "newPpidValue"}
 */
data class UpdateProfileRequest(
    val mpPpid: String
)

/**
 * MAPPING EXTENSIONS: Convert API responses to domain models
 */
fun LoketProfileResponse.toDomain(): com.proyek.maganggsp.domain.model.Loket {
    return com.proyek.maganggsp.domain.model.Loket(
        ppid = this.ppid ?: "",
        namaLoket = this.namaLoket ?: "Unknown",
        nomorHP = this.nomorHP ?: "",
        alamat = this.alamat ?: "",
        email = this.email ?: "",
        status = determineLoketStatus(this.ppid),
        saldoTerakhir = this.saldoTerakhir ?: 0L,
        tanggalAkses = this.tanggalAkses ?: "",
        receipts = this.receipts?.mapNotNull { it.toDomain() } ?: emptyList()
    )
}

fun ReceiptResponse.toDomain(): com.proyek.maganggsp.domain.model.Receipt? {
    if (refNumber.isNullOrBlank() || idPelanggan.isNullOrBlank() || ppid.isNullOrBlank()) {
        return null
    }

    return com.proyek.maganggsp.domain.model.Receipt(
        refNumber = refNumber,
        idPelanggan = idPelanggan,
        tanggal = tanggal ?: "",
        mutasi = mutasi ?: 0L,
        totalSaldo = totalSaldo ?: 0L,
        ppid = ppid
    )
}

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
 * BLOCK/UNBLOCK LOGIC: Determine status from PPID
 */
private fun determineLoketStatus(ppid: String?): com.proyek.maganggsp.domain.model.LoketStatus {
    return if (ppid?.endsWith("blok") == true) {
        com.proyek.maganggsp.domain.model.LoketStatus.BLOCKED
    } else {
        com.proyek.maganggsp.domain.model.LoketStatus.NORMAL
    }
}

/**
 * HELPER: Create block/unblock requests
 */
fun createBlockRequest(originalPpid: String): UpdateProfileRequest {
    val blockedPpid = if (originalPpid.endsWith("blok")) {
        originalPpid // Already blocked
    } else {
        "${originalPpid}blok"
    }
    return UpdateProfileRequest(mpPpid = blockedPpid)
}

fun createUnblockRequest(blockedPpid: String): UpdateProfileRequest {
    val originalPpid = if (blockedPpid.endsWith("blok")) {
        blockedPpid.removeSuffix("blok")
    } else {
        blockedPpid // Not blocked
    }
    return UpdateProfileRequest(mpPpid = originalPpid)
}
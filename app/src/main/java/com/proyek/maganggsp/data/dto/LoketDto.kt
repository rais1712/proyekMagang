// File: app/src/main/java/com/proyek/maganggsp/data/dto/LoketDto.kt - ENHANCED
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

/**
 * ENHANCED: ProfileResponse mapping to comprehensive Loket model
 * API Response structure: GET /profiles/ppid/{ppid}
 */
data class LoketProfileResponse(
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

    @SerializedName("status")
    val status: String?, // "NORMAL", "BLOCKED", "FLAGGED"

    @SerializedName("saldoTerakhir")
    val saldoTerakhir: Long?,

    @SerializedName("tanggalAkses")
    val tanggalAkses: String?,

    // Include receipts in profile response if available
    @SerializedName("receipts")
    val receipts: List<ReceiptResponse>?
)

/**
 * ENHANCED: Receipt response for transactions within loket
 * Can be part of LoketProfileResponse or separate endpoint
 */
data class ReceiptResponse(
    @SerializedName("refNumber")
    val refNumber: String?,

    @SerializedName("idPelanggan")
    val idPelanggan: String?,

    @SerializedName("tanggal")
    val tanggal: String?,

    @SerializedName("mutasi")
    val mutasi: Long?,

    @SerializedName("totalSaldo")
    val totalSaldo: Long?,

    @SerializedName("ppid")
    val ppid: String?,

    @SerializedName("tipeTransaksi")
    val tipeTransaksi: String?
)

/**
 * ENHANCED: Update profile request with additional fields
 * API Request structure: PUT /profiles/ppid/{ppid}
 */
data class UpdateLoketProfileRequest(
    @SerializedName("mpPpid")
    val mpPpid: String,

    @SerializedName("namaLoket")
    val namaLoket: String? = null,

    @SerializedName("nomorHP")
    val nomorHP: String? = null,

    @SerializedName("alamat")
    val alamat: String? = null,

    @SerializedName("email")
    val email: String? = null,

    @SerializedName("status")
    val status: String? = null
)

/**
 * NEW: Manual search request for PPID input
 * Since no search endpoint, support manual PPID entry
 */
data class LoketSearchRequest(
    val ppid: String
) {
    fun isValidPpid(): Boolean = ppid.isNotBlank() && ppid.length >= 5
}


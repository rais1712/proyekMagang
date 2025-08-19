package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

/**
 * FIXED: Updated MutasiDto dengan fields tambahan yang mungkin diperlukan
 * Menambahkan nullable fields untuk flexibility
 */
data class MutasiDto(
    // Core fields (existing)
    @SerializedName("tanggal")
    val tanggal: String?,

    @SerializedName("nomor_referensi")
    val nomorReferensi: String?,

    @SerializedName("nominal_transaksi")
    val nominalTransaksi: Long?,

    @SerializedName("sisa_saldo")
    val sisaSaldo: Long?,

    // Additional fields yang mungkin ada di API response
    @SerializedName("id")
    val id: String?,

    @SerializedName("transaction_type")
    val transactionType: String?,

    @SerializedName("timestamp")
    val timestamp: String?,

    @SerializedName("reference")
    val reference: String?,

    @SerializedName("amount")
    val amount: Long?,

    @SerializedName("balance_after")
    val balanceAfter: Long?
)
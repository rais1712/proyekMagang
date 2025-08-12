package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

data class MutasiDto(
    @SerializedName("tanggal")
    val tanggal: String?,
    @SerializedName("nomor_referensi")
    val nomorReferensi: String?,
    @SerializedName("nominal_transaksi")
    val nominalTransaksi: Long?,
    @SerializedName("sisa_saldo")
    val sisaSaldo: Long?
)
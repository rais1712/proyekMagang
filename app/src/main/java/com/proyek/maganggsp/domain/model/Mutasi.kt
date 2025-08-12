package com.proyek.maganggsp.domain.model

data class Mutasi(
    val tanggal: String,
    val nomorReferensi: String,
    val nominalTransaksi: Long,
    val sisaSaldo: Long
)
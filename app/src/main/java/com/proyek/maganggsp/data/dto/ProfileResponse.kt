package com.proyek.maganggsp.data.dto

import com.proyek.maganggsp.domain.model.Receipt

// DTO untuk response profile dari API /profiles/ppid/{ppid}
data class ProfileResponse(
    val refNumber: String,
    val noPelanggan: String,
    val tipe: String,
    val jumlah: Long,
    val status: String,
    val waktu: String,
    val ppid: String,
    val namaLoket: String = "",
    val nomorHP: String = "",
    val email: String = "",
    val alamat: String = ""
)

fun ProfileResponse.toReceipt(): Receipt {
    return Receipt(
        refNumber = refNumber,
        noPelanggan = noPelanggan,
        tipe = tipe,
        jumlah = jumlah,
        status = status,
        waktu = waktu,
        ppid = ppid,
        namaLoket = namaLoket,
        nomorHP = nomorHP,
        email = email,
        alamat = alamat
    )
}


package com.proyek.maganggsp.data.dto

import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.Mutasi

// Memetakan LoginResponse -> Admin
fun LoginResponse.toDomain(): Admin {
    return Admin(
        name = this.adminName ?: "Nama Admin",
        email = this.adminEmail ?: "Email tidak tersedia",
        token = this.token ?: ""
    )
}

// Memetakan LoketDto -> Loket
fun LoketDto.toDomain(): Loket {
    return Loket(
        noLoket = this.noLoket ?: "-",
        namaLoket = this.namaLoket ?: "Tanpa Nama",
        nomorTelepon = this.nomorTelepon ?: "-",
        email = this.email ?: "Email tidak tersedia",
        status = this.status ?: "UNKNOWN"
    )
}

// Memetakan MutasiDto -> Mutasi
fun MutasiDto.toDomain(): Mutasi {
    return Mutasi(
        tanggal = this.tanggal ?: "-",
        nomorReferensi = this.nomorReferensi ?: "-",
        nominalTransaksi = this.nominalTransaksi ?: 0L,
        sisaSaldo = this.sisaSaldo ?: 0L
    )
}
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

// FIXED: Memetakan MutasiDto -> Mutasi dengan robust mapping
fun MutasiDto.toDomain(): Mutasi {
    // Prioritize fields - gunakan field yang lebih spesifik dulu
    val finalTanggal = this.timestamp ?: this.tanggal ?: "-"
    val finalReferensi = this.reference ?: this.nomorReferensi ?: "-"
    val finalNominal = this.amount ?: this.nominalTransaksi ?: 0L
    val finalSaldo = this.balanceAfter ?: this.sisaSaldo ?: 0L

    return Mutasi(
        tanggal = finalTanggal,
        nomorReferensi = finalReferensi,
        nominalTransaksi = finalNominal,
        sisaSaldo = finalSaldo,

        // New fields akan otomatis terisi dari constructor Mutasi
        id = this.id ?: finalReferensi,
        timestamp = finalTanggal,
        reference = finalReferensi,
        amount = finalNominal,
        balanceAfter = finalSaldo,
        type = this.transactionType ?: if (finalNominal >= 0) "IN" else "OUT"
    )
}
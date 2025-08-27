package com.proyek.maganggsp.data.dto

import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.Mutasi

// FIXED: Memetakan LoginResponse -> Admin berdasarkan format server yang sebenarnya
fun LoginResponse.toDomain(): Admin {
    return Admin(
        // Ambil nama dari email (sebelum @) karena server tidak kirim admin_name
        name = extractNameFromEmail(this.email),
        email = this.email ?: "Email tidak tersedia",
        token = this.token ?: ""
    )
}

/**
 * Helper function untuk extract nama dari email
 * Contoh: "lalan@gsp.co.id" -> "Lalan" (dengan capitalize)
 */
private fun extractNameFromEmail(email: String?): String {
    return if (!email.isNullOrBlank() && email.contains("@")) {
        val localPart = email.substringBefore("@")
        // Capitalize first letter dan ganti underscore/dot dengan space
        localPart.replace("[._]".toRegex(), " ")
            .split(" ")
            .joinToString(" ") { word ->
                word.replaceFirstChar {
                    if (it.isLowerCase()) it.titlecase() else it.toString()
                }
            }
    } else {
        "Admin User"
    }
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
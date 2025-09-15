// File: app/src/main/java/com/proyek/maganggsp/data/dto/LoketMapper.kt - ENHANCED
package com.proyek.maganggsp.data.dto

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.LoketStatus

/**
 * ENHANCED: Mapping functions for Loket management
 */

// LoketProfileResponse -> Loket mapping
fun LoketProfileResponse.toDomain(): Loket {
    return Loket(
        ppid = this.ppid ?: "",
        namaLoket = this.namaLoket ?: "Loket Tidak Dikenal",
        nomorHP = this.nomorHP ?: "-",
        alamat = this.alamat ?: "-",
        email = this.email ?: "-",
        status = parseLoketStatus(this.status),
        saldoTerakhir = this.saldoTerakhir ?: 0L,
        tanggalAkses = this.tanggalAkses ?: "",
        receipts = this.receipts?.mapNotNull { it.toDomain() } ?: emptyList()
    )
}

// ReceiptResponse -> Receipt mapping
fun ReceiptResponse.toDomain(): Receipt? {
    // Validate required fields
    val refNum = this.refNumber
    val idPel = this.idPelanggan
    val ppidVal = this.ppid

    if (refNum.isNullOrBlank() || idPel.isNullOrBlank() || ppidVal.isNullOrBlank()) {
        return null
    }

    return Receipt(
        refNumber = refNum,
        idPelanggan = idPel,
        tanggal = this.tanggal ?: "",
        mutasi = this.mutasi ?: 0L,
        totalSaldo = this.totalSaldo ?: 0L,
        ppid = ppidVal,
        tipeTransaksi = this.tipeTransaksi ?: "Receipt"
    )
}

// Status parsing helper
private fun parseLoketStatus(status: String?): LoketStatus {
    return when (status?.uppercase()) {
        "BLOCKED", "DIBLOKIR" -> LoketStatus.BLOCKED
        "FLAGGED", "DITANDAI" -> LoketStatus.FLAGGED
        else -> LoketStatus.NORMAL
    }
}

// Loket -> LoketProfileResponse mapping (for updates)
fun Loket.toUpdateRequest(): UpdateLoketProfileRequest {
    return UpdateLoketProfileRequest(
        mpPpid = this.ppid,
        namaLoket = this.namaLoket,
        nomorHP = this.nomorHP,
        alamat = this.alamat,
        email = this.email,
        status = this.status.name
    )
}
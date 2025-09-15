// File: app/src/main/java/com/proyek/maganggsp/data/dto/LoketMapper.kt - ENHANCED
package com.proyek.maganggsp.data.dto

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.LoketStatus
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.model.TransactionLog

/**
 * ENHANCED: Mapping functions for Loket management
 */

data class LoketResponse(
    val ppid: String,
    val namaLoket: String,
    val nomorHP: String,
    val alamat: String,
    val email: String,
    val status: String
)

data class LoketProfileResponse(
    val ppid: String,
    val namaLoket: String,
    val nomorHP: String,
    val alamat: String,
    val email: String,
    val status: String,
    val saldoTerakhir: Long = 0L,
    val tanggalAkses: String = "",
    val receipts: List<ReceiptResponse> = emptyList()
)

data class ReceiptResponse(
    val refNumber: String,
    val idPelanggan: String,
    val tanggal: String,
    val mutasi: Long,
    val totalSaldo: Long,
    val ppid: String
)

data class TransactionResponse(
    val tldRefnum: String,
    val tldPan: String,
    val tldIdpel: String,
    val tldAmount: Long,
    val tldBalance: Long,
    val tldDate: String,
    val tldPpid: String
)

fun LoketResponse.toDomain(): Loket {
    return Loket(
        ppid = ppid,
        namaLoket = namaLoket,
        nomorHP = nomorHP,
        alamat = alamat,
        email = email,
        status = when(status.uppercase()) {
            "BLOCKED" -> LoketStatus.BLOCKED
            else -> LoketStatus.NORMAL
        }
    )
}

fun LoketProfileResponse.toDomain(): Loket {
    return Loket(
        ppid = ppid,
        namaLoket = namaLoket,
        nomorHP = nomorHP,
        alamat = alamat,
        email = email,
        status = when(status.uppercase()) {
            "BLOCKED" -> LoketStatus.BLOCKED
            "FLAGGED" -> LoketStatus.FLAGGED
            else -> LoketStatus.NORMAL
        },
        saldoTerakhir = saldoTerakhir,
        tanggalAkses = tanggalAkses,
        receipts = receipts.map { it.toDomain() }
    )
}

fun ReceiptResponse.toDomain(): Receipt {
    return Receipt(
        refNumber = refNumber,
        idPelanggan = idPelanggan,
        tanggal = tanggal,
        mutasi = mutasi,
        totalSaldo = totalSaldo,
        ppid = ppid
    )
}

fun TransactionResponse.toDomain(): TransactionLog {
    return TransactionLog(
        tldRefnum = tldRefnum,
        tldPan = tldPan,
        tldIdpel = tldIdpel,
        tldAmount = tldAmount,
        tldBalance = tldBalance,
        tldDate = tldDate,
        tldPpid = tldPpid
    )
}

fun List<TransactionResponse>.toDomain(): List<TransactionLog> = map { it.toDomain() }

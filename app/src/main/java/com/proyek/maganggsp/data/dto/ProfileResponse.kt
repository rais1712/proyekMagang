// File: app/src/main/java/com/proyek/maganggsp/data/dto/ProfileResponse.kt
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.model.LoketStatus

/**
 * Response DTO untuk GET /profiles/ppid/{ppid}
 */
data class ProfileResponse(
    @SerializedName("refNumber") val refNumber: String = "",
    @SerializedName("noPelanggan") val noPelanggan: String = "",
    @SerializedName("tipe") val tipe: String = "",
    @SerializedName("jumlah") val jumlah: Long = 0L,
    @SerializedName("status") val status: String = "",
    @SerializedName("waktu") val waktu: String = "",
    @SerializedName("ppid") val ppid: String = "",
    @SerializedName("namaLoket") val namaLoket: String = "",
    @SerializedName("nomorHP") val nomorHP: String = "",
    @SerializedName("email") val email: String = "",
    @SerializedName("alamat") val alamat: String = "",
    @SerializedName("loketStatus") val loketStatus: String = "normal"
) {
    /**
     * Convert DTO to domain model
     */
    fun toReceipt(): Receipt {
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
            alamat = alamat,
            loketStatus = LoketStatus.fromString(loketStatus)
        )
    }
}

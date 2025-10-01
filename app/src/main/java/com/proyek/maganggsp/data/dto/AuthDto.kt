// File: app/src/main/java/com/proyek/maganggsp/data/dto/AuthDto.kt - FIXED WITH MAPPING
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName
import com.proyek.maganggsp.domain.model.Admin

// Permintaan login
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

// Hapus deklarasi data class LoginResponse dan extension function mapping ke Admin di file ini

/**
 * PROFILE REPOSITORY MAPPING FIX
 */
data class ProfileResponse(
    @SerializedName("ppid") val ppid: String?,
    @SerializedName("namaLoket") val namaLoket: String?,
    @SerializedName("nomorHP") val nomorHP: String?,
    @SerializedName("alamat") val alamat: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("saldoTerakhir") val saldoTerakhir: Long?,
    @SerializedName("tanggalAkses") val tanggalAkses: String?
)

/**
 * CRITICAL: ProfileResponse to Receipt mapping for ProfileRepository
 */
fun ProfileResponse.toDomain(): com.proyek.maganggsp.domain.model.Receipt {
    return com.proyek.maganggsp.domain.model.Receipt(
        refNumber = "PROFILE-${this.ppid}",
        idPelanggan = this.ppid ?: "",
        tanggal = this.tanggalAkses ?: "",
        mutasi = this.saldoTerakhir ?: 0L,
        totalSaldo = this.saldoTerakhir ?: 0L,
        ppid = this.ppid ?: "",
        tipeTransaksi = "Profile"
    )
}

data class UpdateProfileRequest(
    @SerializedName("mpPpid") val mpPpid: String
)
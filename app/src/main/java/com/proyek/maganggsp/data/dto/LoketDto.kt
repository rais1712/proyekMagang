package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

// DTO untuk data loket dari API
data class LoketDto(
    @SerializedName("no_loket")
    val noLoket: String?,
    @SerializedName("nama_loket")
    val namaLoket: String?,
    @SerializedName("nomor_telepon")
    val nomorTelepon: String?,
    @SerializedName("email")
    val email: String?,
    @SerializedName("status")
    val status: String?
)
// File: app/src/main/java/com/proyek/maganggsp/data/dto/ProfileResponse.kt

package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

data class ProfileResponse(
    @SerializedName("ppid")
    val ppid: String?,

    @SerializedName("nama")
    val nama: String?,

    @SerializedName("alamat")
    val alamat: String?,

    @SerializedName("no_hp")
    val noHp: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("status")
    val status: String?,

    @SerializedName("saldo")
    val saldo: Long?
)

// File: app/src/main/java/com/proyek/maganggsp/data/dto/UpdateProfileRequest.kt

package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

data class UpdateProfileRequest(
    @SerializedName("nama")
    val nama: String?,

    @SerializedName("alamat")
    val alamat: String?,

    @SerializedName("no_hp")
    val noHp: String?
)

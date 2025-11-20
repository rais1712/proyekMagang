// File: app/src/main/java/com/proyek/maganggsp/data/dto/LoginResponse.kt
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

data class LoginResponse(
    @SerializedName("token")
    val token: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("nama")
    val nama: String?
)

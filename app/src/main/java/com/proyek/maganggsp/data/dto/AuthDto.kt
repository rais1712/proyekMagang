package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

// Permintaan login
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

// FIXED: Respons login sesuai dengan format server yang sebenarnya
data class LoginResponse(
    @SerializedName("token")
    val token: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("role")
    val role: String?
)
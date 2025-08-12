package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

// Permintaan login
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

// Respons login
data class LoginResponse(
    @SerializedName("admin_name")
    val adminName: String?,
    @SerializedName("admin_email")
    val adminEmail: String?,
    @SerializedName("token")
    val token: String?
)
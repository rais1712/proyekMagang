// File: app/src/main/java/com/proyek/maganggsp/data/dto/AuthDto.kt
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    val email: String,
    val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String,
    @SerializedName("admin_data") val adminData: AdminDto
)


// DTO khusus untuk struktur data admin yang datang dari API
data class AdminDto(
    @SerializedName("id") val id: String,
    @SerializedName("full_name") val fullName: String,
    @SerializedName("email") val email: String
)
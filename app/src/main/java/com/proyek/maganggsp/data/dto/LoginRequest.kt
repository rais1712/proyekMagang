// File: app/src/main/java/com/proyek/maganggsp/data/dto/LoginRequest.kt

package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

data class LoginRequest(
    @SerializedName("email")
    val email: String,

    @SerializedName("password")
    val password: String
)

// File: app/src/main/java/com/proyek/maganggsp/data/api/LoginRequest.kt
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

/**
 * MODULAR: Login request data class
 * Used by AuthApi for login operations
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)
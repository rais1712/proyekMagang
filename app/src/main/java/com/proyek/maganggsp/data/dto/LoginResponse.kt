// File: app/src/main/java/com/proyek/maganggsp/data/api/LoginResponse.kt
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName
import com.proyek.maganggsp.domain.model.Admin

/**
 * MODULAR: Login response data class
 * Maps to Admin domain model
 */
data class LoginResponse(
    @SerializedName("token")
    val token: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("role")
    val role: String?
)

/**
 * Extension function to map API response to domain model
 */
fun LoginResponse.toAdmin(): Admin {
    return Admin(
        name = this.email?.substringBefore("@") ?: "Admin",
        email = this.email ?: "",
        token = this.token ?: "",
        role = this.role ?: "admin"
    )
}
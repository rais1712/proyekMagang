package com.proyek.maganggsp.domain.model

/**
 * ✅ PHASE 1: Enhanced Admin model with utility functions
 */
data class Admin(
    val name: String,
    val email: String,
    val token: String,
    val role: String = "admin" // Default role
) {

    // Utility functions for Admin model
    fun isValidToken(): Boolean = token.isNotBlank() && token.length > 10

    fun getDisplayName(): String = if (name.isNotBlank()) name else email.substringBefore("@")

    fun hasValidCredentials(): Boolean = name.isNotBlank() && email.isNotBlank() && isValidToken()

    fun toDebugString(): String = "Admin(name='$name', email='$email', tokenLength=${token.length})"
}

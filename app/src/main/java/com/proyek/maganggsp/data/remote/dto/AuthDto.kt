package com.proyek.maganggsp.data.remote.dto

/**
 * DTO untuk response dari endpoint login
 */
data class LoginResponseDto(
    val token: String,
    val admin: AdminDto
)

data class AdminDto(
    val id: String,
    val email: String,
    val name: String,
    val role: String
)

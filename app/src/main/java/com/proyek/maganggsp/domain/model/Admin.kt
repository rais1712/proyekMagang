package com.proyek.maganggsp.domain.model

// Model sederhana untuk admin yang login
data class Admin(
    val name: String,
    val email: String,
    val token: String
)
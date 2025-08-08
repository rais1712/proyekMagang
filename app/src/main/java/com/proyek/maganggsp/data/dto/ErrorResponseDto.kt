package com.proyek.maganggsp.data.remote.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO untuk menangkap struktur JSON dari respons error API.
 */
data class ErrorResponseDto(
    @SerializedName("message") val message: String?
)
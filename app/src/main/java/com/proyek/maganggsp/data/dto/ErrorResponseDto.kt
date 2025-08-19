// FIX: Corrected package path to match directory structure
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

/**
 * DTO untuk menangkap struktur JSON dari respons error API.
 */
data class ErrorResponseDto(
    @SerializedName("message") val message: String?
)
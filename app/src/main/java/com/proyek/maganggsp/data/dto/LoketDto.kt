// File: app/src/main/java/com/proyek/maganggsp/data/dto/LoketDto.kt
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

// DTO untuk data loket dari API
data class LoketDto(
    @SerializedName("id") val id: String,
    @SerializedName("loket_name") val loketName: String,
    @SerializedName("phone_number") val phoneNumber: String,
    @SerializedName("address") val address: String,
    @SerializedName("last_balance") val lastBalance: Long,
    @SerializedName("current_status") val currentStatus: String,
    @SerializedName("last_accessed_date") val lastAccessedDate: String? = null // <<< TAMBAHKAN FIELD INI

)
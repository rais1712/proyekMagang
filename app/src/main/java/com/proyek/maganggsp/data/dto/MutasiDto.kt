// File: app/src/main/java/com/proyek/maganggsp/data/dto/MutasiDto.kt
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

// DTO untuk data mutasi dari API
data class MutasiDto(
    @SerializedName("transaction_id") val transactionId: String,
    @SerializedName("notes") val notes: String,
    @SerializedName("amount") val amount: Long,
    @SerializedName("transaction_type") val transactionType: String, // "IN" atau "OUT"
    @SerializedName("balance_after") val balanceAfter: Long,
    @SerializedName("reference_code") val referenceCode: String,
    @SerializedName("transaction_date") val transactionDate: String,
    @SerializedName("is_flagged") val isFlagged: Boolean
)
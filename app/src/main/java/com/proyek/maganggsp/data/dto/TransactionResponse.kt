// File: app/src/main/java/com/proyek/maganggsp/data/dto/TransactionResponse.kt
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName
import com.proyek.maganggsp.domain.model.TransactionLog

/**
 * Response DTO untuk GET /trx/ppid/{ppid}
 */
data class TransactionResponse(
    @SerializedName("id") val id: String = "",
    @SerializedName("refNumber") val refNumber: String = "",
    @SerializedName("noPelanggan") val noPelanggan: String = "",
    @SerializedName("amount") val amount: Long = 0L,
    @SerializedName("type") val type: String = "", // "CREDIT" atau "DEBIT"
    @SerializedName("timestamp") val timestamp: String = "",
    @SerializedName("description") val description: String = "",
    @SerializedName("status") val status: String = "",
    @SerializedName("ppid") val ppid: String = "",
    @SerializedName("saldo") val saldo: Long = 0L
) {
    /**
     * Convert DTO to domain model
     */
    fun toTransactionLog(): TransactionLog {
        return TransactionLog(
            id = id,
            refNumber = refNumber,
            noPelanggan = noPelanggan,
            amount = amount,
            type = type,
            timestamp = timestamp,
            description = description,
            status = status,
            ppid = ppid,
            saldo = saldo
        )
    }
}

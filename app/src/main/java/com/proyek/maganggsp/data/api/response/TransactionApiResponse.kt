// File: app/src/main/java/com/proyek/maganggsp/data/api/response/TransactionApiResponse.kt
package com.proyek.maganggsp.data.api.response

import com.google.gson.annotations.SerializedName
import com.proyek.maganggsp.domain.model.TransactionLog

/**
 * MODULAR: Transaction API Response
 * Maps to TransactionLog domain model
 */
data class TransactionApiResponse(
    @SerializedName("tldRefnum")
    val tldRefnum: String?,

    @SerializedName("tldPan")
    val tldPan: String?,

    @SerializedName("tldIdpel")
    val tldIdpel: String?,

    @SerializedName("tldAmount")
    val tldAmount: Long?,

    @SerializedName("tldBalance")
    val tldBalance: Long?,

    @SerializedName("tldDate")
    val tldDate: String?,

    @SerializedName("tldPpid")
    val tldPpid: String?
)

/**
 * Extension function to map API response to domain model
 */
fun TransactionApiResponse.toTransactionLog(): TransactionLog {
    return TransactionLog(
        tldRefnum = tldRefnum ?: "",
        tldPan = tldPan ?: "",
        tldIdpel = tldIdpel ?: "",
        tldAmount = tldAmount ?: 0L,
        tldBalance = tldBalance ?: 0L,
        tldDate = tldDate ?: "",
        tldPpid = tldPpid ?: ""
    )
}




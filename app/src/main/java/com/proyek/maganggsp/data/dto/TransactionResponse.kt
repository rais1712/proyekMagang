// File: app/src/main/java/com/proyek/maganggsp/data/dto/TransactionResponse.kt

package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

data class TransactionResponse(
    @SerializedName("tld_refnum")
    val tldRefnum: String?,

    @SerializedName("tld_pan")
    val tldPan: String?,

    @SerializedName("tld_idpel")
    val tldIdpel: String?,

    @SerializedName("tld_amount")
    val tldAmount: Long?,

    @SerializedName("tld_balance")
    val tldBalance: Long?,

    @SerializedName("tld_date")
    val tldDate: String?,

    @SerializedName("tld_ppid")
    val tldPpid: String?
)

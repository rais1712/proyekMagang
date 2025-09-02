// File: app/src/main/java/com/proyek/maganggsp/data/dto/ProfileDto.kt
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

/**
 * NEW DTO: ProfileResponse untuk mapping dari /profiles/ppid/{ppid} ke Receipt model
 * API Response structure yang akan di-map ke Receipt domain model
 */
data class ProfileResponse(
    @SerializedName("refNumber")
    val refNumber: String?,

    @SerializedName("idPelanggan")
    val idPelanggan: String?,

    @SerializedName("amount")
    val amount: Long?,

    @SerializedName("logged")
    val logged: String?
)

/**
 * NEW DTO: TransactionResponse untuk mapping dari /trx/ppid/{ppid} ke TransactionLog model
 * API Response structure untuk transaction logs
 */
data class TransactionResponse(
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

    // NOTE: message field NOT included as per requirements
)

/**
 * NEW DTO: UpdateProfileRequest untuk PUT /profiles/ppid/{ppid}
 * Request body untuk update profile
 */
data class UpdateProfileRequest(
    @SerializedName("mpPpid")
    val mpPpid: String
)
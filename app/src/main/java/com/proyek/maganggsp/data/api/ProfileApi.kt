// File: app/src/main/java/com/proyek/maganggsp/data/api/ProfileApi.kt - UNIFIED API
package com.proyek.maganggsp.data.api

import retrofit2.Response
import retrofit2.http.*

/**
 * UNIFIED API: Single source of truth for all profile operations
 * Eliminates LoketApi confusion - uses real backend endpoints
 */
interface ProfileApi {

    /**
     * PRIMARY: Get profile data (maps to Receipt for home screen)
     * URL: GET /api/profiles/ppid/{ppid}
     */
    @GET("profiles/ppid/{ppid}")
    suspend fun getProfile(@Path("ppid") ppid: String): ProfileResponse

    /**
     * PRIMARY: Get transaction logs (for detail screen)
     * URL: GET /api/trx/ppid/{ppid}
     */
    @GET("trx/ppid/{ppid}")
    suspend fun getTransactions(@Path("ppid") ppid: String): List<TransactionResponse>

    /**
     * PRIMARY: Update profile (block/unblock operations)
     * URL: PUT /api/profiles/ppid/{ppid}
     * Body: {"mpPpid": "PIDLKTD0025blok"} for block
     * Body: {"mpPpid": "PIDLKTD0025"} for unblock
     */
    @PUT("profiles/ppid/{ppid}")
    suspend fun updateProfile(
        @Path("ppid") ppid: String,
        @Body request: UpdateProfileRequest
    ): Response<Unit>
}

// File: app/src/main/java/com/proyek/maganggsp/data/dto/ProfileDto.kt - UNIFIED DTOs
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName

/**
 * UNIFIED DTO: Profile response structure
 */
data class ProfileResponse(
    @SerializedName("ppid") val ppid: String?,
    @SerializedName("namaLoket") val namaLoket: String?,
    @SerializedName("nomorHP") val nomorHP: String?,
    @SerializedName("alamat") val alamat: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("saldoTerakhir") val saldoTerakhir: Long?,
    @SerializedName("tanggalAkses") val tanggalAkses: String?
)

/**
 * UNIFIED DTO: Transaction response structure
 */
data class TransactionResponse(
    @SerializedName("tldRefnum") val tldRefnum: String?,
    @SerializedName("tldPan") val tldPan: String?,
    @SerializedName("tldIdpel") val tldIdpel: String?,
    @SerializedName("tldAmount") val tldAmount: Long?,
    @SerializedName("tldBalance") val tldBalance: Long?,
    @SerializedName("tldDate") val tldDate: String?,
    @SerializedName("tldPpid") val tldPpid: String?
)

/**
 * UPDATE REQUEST: For block/unblock operations
 */
data class UpdateProfileRequest(
    @SerializedName("mpPpid") val mpPpid: String
)

// DOMAIN MAPPING EXTENSIONS
import com.proyek.maganggsp.domain.model.*

/**
 * Profile to Receipt mapping (for home screen)
 */
fun ProfileResponse.toReceipt(): Receipt {
    return Receipt(
        refNumber = "PROFILE-${this.ppid ?: "UNKNOWN"}",
        idPelanggan = this.ppid ?: "",
        amount = this.saldoTerakhir ?: 0L,
        logged = this.tanggalAkses ?: "",
        ppid = this.ppid ?: "",
        namaLoket = this.namaLoket ?: "",
        nomorHP = this.nomorHP ?: "",
        email = this.email ?: "",
        alamat = this.alamat ?: "",
        saldoTerakhir = this.saldoTerakhir ?: 0L,
        tanggalAkses = this.tanggalAkses ?: ""
    )
}

/**
 * Profile to LoketProfile mapping (for detail screen)
 */
fun ProfileResponse.toLoketProfile(): LoketProfile {
    return LoketProfile(
        ppid = this.ppid ?: "",
        namaLoket = this.namaLoket ?: "Unknown Loket",
        nomorHP = this.nomorHP ?: "",
        alamat = this.alamat ?: "",
        email = this.email ?: "",
        status = LoketStatus.fromPpid(this.ppid),
        saldoTerakhir = this.saldoTerakhir ?: 0L,
        tanggalAkses = this.tanggalAkses ?: ""
    )
}

/**
 * Transaction response to domain mapping
 */
fun TransactionResponse.toTransactionLog(): TransactionLog {
    return TransactionLog(
        tldRefnum = this.tldRefnum ?: "",
        tldPan = this.tldPan ?: "",
        tldIdpel = this.tldIdpel ?: "",
        tldAmount = this.tldAmount ?: 0L,
        tldBalance = this.tldBalance ?: 0L,
        tldDate = this.tldDate ?: "",
        tldPpid = this.tldPpid ?: ""
    )
}

/**
 * Block/Unblock request helpers
 */
fun createBlockRequest(originalPpid: String): UpdateProfileRequest {
    val blockedPpid = if (originalPpid.endsWith("blok")) {
        originalPpid // Already blocked
    } else {
        "${originalPpid}blok"
    }
    return UpdateProfileRequest(mpPpid = blockedPpid)
}

fun createUnblockRequest(blockedPpid: String): UpdateProfileRequest {
    val originalPpid = if (blockedPpid.endsWith("blok")) {
        blockedPpid.removeSuffix("blok")
    } else {
        blockedPpid // Not blocked
    }
    return UpdateProfileRequest(mpPpid = originalPpid)
}

// File: app/src/main/java/com/proyek/maganggsp/data/dto/AuthDto.kt - KEEP EXISTING
package com.proyek.maganggsp.data.dto

import com.google.gson.annotations.SerializedName
import com.proyek.maganggsp.domain.model.Admin

/**
 * UNCHANGED: Keep existing login functionality
 */
data class LoginRequest(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String
)

data class LoginResponse(
    @SerializedName("token") val token: String?,
    @SerializedName("email") val email: String?,
    @SerializedName("role") val role: String?
)

fun LoginResponse.toDomain(): Admin {
    return Admin(
        name = this.email?.substringBefore("@") ?: "Admin",
        email = this.email ?: "",
        token = this.token ?: "",
        role = this.role ?: "admin"
    )
}
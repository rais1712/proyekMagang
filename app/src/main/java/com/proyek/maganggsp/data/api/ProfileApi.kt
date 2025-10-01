// File: app/src/main/java/com/proyek/maganggsp/data/api/ProfileApi.kt
package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.ProfileResponse
import com.proyek.maganggsp.data.dto.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.*

/**
 * MODULAR: ProfileApi interface
 * Based on actual HTTP request: GET /profiles/ppid/{ppid}
 * Maps to Receipt domain model for profile display
 */
interface ProfileApi {

    /**
     * Get profile information for profile card display
     * Endpoint: GET /profiles/ppid/{ppid}
     * Example: /profiles/ppid/PIDLKTD0025 or /profiles/ppid/0000001
     * Maps to: Receipt domain model
     */
    @GET("profiles/ppid/{ppid}")
    suspend fun getProfile(@Path("ppid") ppid: String): ProfileResponse

    /**
     * Update profile (for block/unblock operations)
     * Endpoint: PUT /profiles/ppid/{ppid}
     * Body: {"mpPpid": "PIDLKTD0025blok"} for block
     *       {"mpPpid": "PIDLKTD0025"} for unblock
     */
    @PUT("profiles/ppid/{ppid}")
    suspend fun updateProfile(
        @Path("ppid") ppid: String,
        @Body request: UpdateProfileRequest
    ): Response<Unit>
}

// File: app/src/main/java/com/proyek/maganggsp/data/api/TransactionApi.kt
package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.TransactionResponse
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * MODULAR: TransactionApi interface
 * Based on actual HTTP request: GET /trx/ppid/{ppid}
 * Maps to TransactionLog domain model
 */
interface TransactionApi {

    /**
     * Get transaction logs for specific PPID
     * Endpoint: GET /trx/ppid/{ppid}
     * Example: /trx/ppid/PIDLKTD0014
     * Maps to: TransactionLog domain model
     */
    @GET("trx/ppid/{ppid}")
    suspend fun getTransactionLogs(@Path("ppid") ppid: String): List<TransactionResponse>
}

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

// File: app/src/main/java/com/proyek/maganggsp/data/api/LoginRequest.kt
package com.proyek.maganggsp.data.api

import com.google.gson.annotations.SerializedName

/**
 * MODULAR: Login request data class
 * Used by AuthApi for login operations
 */
data class LoginRequest(
    @SerializedName("email")
    val email: String,
    @SerializedName("password")
    val password: String
)

// File: app/src/main/java/com/proyek/maganggsp/data/api/LoginResponse.kt
package com.proyek.maganggsp.data.api

import com.google.gson.annotations.SerializedName
import com.proyek.maganggsp.domain.model.Admin

/**
 * MODULAR: Login response data class
 * Maps to Admin domain model
 */
data class LoginResponse(
    @SerializedName("token")
    val token: String?,

    @SerializedName("email")
    val email: String?,

    @SerializedName("role")
    val role: String?
)

/**
 * Extension function to map API response to domain model
 */
fun LoginResponse.toAdmin(): Admin {
    return Admin(
        name = this.email?.substringBefore("@") ?: "Admin",
        email = this.email ?: "",
        token = this.token ?: "",
        role = this.role ?: "admin"
    )
}
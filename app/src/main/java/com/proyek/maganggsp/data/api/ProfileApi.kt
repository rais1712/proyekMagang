// File: app/src/main/java/com/proyek/maganggsp/data/api/ProfileApi.kt
package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.ProfileResponse
import com.proyek.maganggsp.data.dto.TransactionResponse
import com.proyek.maganggsp.data.dto.UpdateProfileRequest
import retrofit2.Response
import retrofit2.http.*

interface ProfileApi {
    @GET("profiles/ppid/{ppid}")
    suspend fun getProfile(@Path("ppid") ppid: String): ProfileResponse

    @GET("trx/ppid/{ppid}")
    suspend fun getTransactions(@Path("ppid") ppid: String): List<TransactionResponse>

    @PUT("profiles/ppid/{ppid}")
    suspend fun updateProfile(
        @Path("ppid") ppid: String,
        @Body request: UpdateProfileRequest
    ): Response<Unit>
}
package com.proyek.maganggsp.data.remote.api

import com.proyek.maganggsp.data.remote.dto.LoketDto
import com.proyek.maganggsp.data.remote.dto.MutasiDto
import retrofit2.Response
import retrofit2.http.*

/**
 * Interface untuk endpoint-endpoint terkait Loket
 */
interface LoketApi {
    @GET("loket/{phoneNumber}")
    suspend fun getLoketByPhone(
        @Path("phoneNumber") phoneNumber: String
    ): Response<LoketDto>

    @GET("loket/{id}/mutations")
    suspend fun getLoketMutations(
        @Path("id") loketId: String
    ): Response<List<MutasiDto>>

    @POST("loket/{id}/block")
    suspend fun blockLoket(
        @Path("id") loketId: String
    ): Response<Unit>

    @POST("loket/{id}/unblock")
    suspend fun unblockLoket(
        @Path("id") loketId: String
    ): Response<Unit>

    @GET("loket/flagged")
    suspend fun getFlaggedLokets(): Response<List<LoketDto>>

    @GET("loket/blocked")
    suspend fun getBlockedLokets(): Response<List<LoketDto>>

    @POST("mutations/{id}/flag")
    suspend fun flagTransaction(@Path("id") mutationId: String): Response<Unit>
}

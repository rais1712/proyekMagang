package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.LoketDto
import com.proyek.maganggsp.data.dto.MutasiDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface LoketApi {

    @GET("loket/{idLoket}")
    suspend fun getLoketDetail(
        @Path("idLoket") idLoket: String
    ): LoketDto // Langsung mengembalikan DTO

    @GET("mutasi/{idLoket}")
    suspend fun getMutation(
        @Path("idLoket") idLoket: String
    ): List<MutasiDto> // Langsung mengembalikan List DTO

    @GET("search")
    suspend fun searchLoket(
        @Query("q") query: String
    ): List<LoketDto> // Langsung mengembalikan List DTO

    @POST("loket/{idLoket}/block")
    suspend fun blockLoket(
        @Path("idLoket") idLoket: String
    ): Response<Unit> // Menggunakan Response<Unit> untuk respons tanpa body

    @POST("loket/{idLoket}/unblock")
    suspend fun unblockLoket(
        @Path("idLoket") idLoket: String
    ): Response<Unit> // Menggunakan Response<Unit> untuk respons tanpa body

    @POST("mutasi/{idMutasi}/flag")
    suspend fun flagMutation(
        @Path("idMutasi") idMutasi: String
    ): Response<Unit>

    @POST("loket/{idLoket}/clear-flags")
    suspend fun clearAllFlags(
        @Path("idLoket") idLoket: String
    ): Response<Unit>

    @GET("loket/flagged")
    suspend fun getFlaggedLokets(): List<LoketDto>

    @GET("loket/blocked")
    suspend fun getBlockedLokets(): List<LoketDto>
}
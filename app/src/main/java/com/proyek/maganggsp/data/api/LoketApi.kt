// File: app/src/main/java/com/proyek/maganggsp/data/api/LoketApi.kt
package com.proyek.maganggsp.data.api

import com.proyek.maganggsp.data.dto.LoketDto
import com.proyek.maganggsp.data.dto.MutasiDto
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.DELETE


interface LoketApi {
    /**
     * Mencari dan mendapatkan detail loket berdasarkan nomor telepon.
     */
    @GET("loket/{phoneNumber}")
    suspend fun getLoketDetails(@Path("phoneNumber") phoneNumber: String): LoketDto

    /**
     * Mendapatkan daftar transaksi (mutasi) dari sebuah loket.
     */
    @GET("loket/{id}/mutations")
    suspend fun getMutations(@Path("id") loketId: String): List<MutasiDto>

    /**
     * Memblokir sebuah loket.
     */
    @POST("loket/{id}/block")
    suspend fun blockLoket(@Path("id") loketId: String): Response<Unit> // Response<Unit> untuk respons kosong

    /**
     * Membuka blokir sebuah loket.
     */
    @POST("loket/{id}/unblock")
    suspend fun unblockLoket(@Path("id") loketId: String): Response<Unit>

    /**
     * Menandai sebuah transaksi sebagai mencurigakan.
     */
    @POST("mutations/{id}/flag")
    suspend fun flagMutation(@Path("id") mutationId: String): Response<Unit>

    /**
     * Mendapatkan daftar loket yang punya transaksi ditandai.
     */
    @GET("loket/flagged")
    suspend fun getFlaggedLokets(): List<LoketDto>

    /**
     * Mendapatkan daftar loket yang diblokir.
     */
    @GET("loket/blocked")
    suspend fun getBlockedLokets(): List<LoketDto>

    @GET("loket/search") // <<< TAMBAHKAN FUNGSI INI
    suspend fun searchLoket(@Query("q") query: String): List<LoketDto>

    @DELETE("loket/{id}/mutations/flags") // <<< TAMBAHKAN FUNGSI INI
    suspend fun clearAllFlags(@Path("id") loketId: String): Response<Unit>
}
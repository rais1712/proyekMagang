package com.proyek.maganggsp.data.api

import retrofit2.http.GET
import retrofit2.http.Path
import com.proyek.maganggsp.data.api.response.TransactionApiResponse

interface TransactionApi {
    @GET("trx/ppid/{ppid}")
    suspend fun getTransactionLogs(@Path("ppid") ppid: String): List<TransactionApiResponse>
}


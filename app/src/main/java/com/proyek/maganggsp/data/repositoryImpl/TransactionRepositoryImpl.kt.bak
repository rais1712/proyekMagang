// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/TransactionRepositoryImpl.kt
package com.proyek.maganggsp.data.repositoryImpl

import com.proyek.maganggsp.data.api.TransactionApi
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.repository.TransactionRepository
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import java.lang.Exception
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Implementation untuk TransactionRepository
 */
@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val api: TransactionApi
) : TransactionRepository {

    override suspend fun getTransactionLogs(ppid: String): Flow<Resource<List<TransactionLog>>> = flow {
        try {
            emit(Resource.Loading<List<TransactionLog>>())

            // TODO: Get token from shared preferences/session
            val token = "Bearer sample_token"

            val response = api.getTransactionLogs(ppid, token)

            if (response.isSuccessful && response.body() != null) {
                val transactions = response.body()!!.map { it.toTransactionLog() }
                emit(Resource.Success<List<TransactionLog>>(transactions))
            } else {
                emit(Resource.Error<List<TransactionLog>>("Failed to load transactions"))
            }
        } catch (e: Exception) {
            emit(Resource.Error<List<TransactionLog>>(e.message ?: "Unknown error"))
        }
    }

    override suspend fun getTransactionLogsWithFilter(
        ppid: String,
        startDate: String?,
        endDate: String?
    ): Flow<Resource<List<TransactionLog>>> = flow {
        try {
            emit(Resource.Loading<List<TransactionLog>>())

            // TODO: Get token from shared preferences/session
            val token = "Bearer sample_token"

            val response = api.getTransactionLogsWithFilter(ppid, startDate, endDate, null, token)

            if (response.isSuccessful && response.body() != null) {
                val transactions = response.body()!!.map { it.toTransactionLog() }
                emit(Resource.Success<List<TransactionLog>>(transactions))
            } else {
                emit(Resource.Error<List<TransactionLog>>("Failed to load transactions"))
            }
        } catch (e: Exception) {
            emit(Resource.Error<List<TransactionLog>>(e.message ?: "Unknown error"))
        }
    }
}

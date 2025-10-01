// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/TransactionRepositoryImpl.kt
package com.proyek.maganggsp.data.repositoryImpl

import android.util.Log
import com.proyek.maganggsp.data.api.TransactionApi
import com.proyek.maganggsp.data.api.response.toTransactionLog
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.repository.TransactionRepository
import com.proyek.maganggsp.domain.repository.TransactionSummary
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TransactionRepositoryImpl @Inject constructor(
    private val api: TransactionApi,
    exceptionMapper: ExceptionMapper
) : BaseRepository(exceptionMapper), TransactionRepository {

    companion object {
        private const val TAG = "TransactionRepositoryImpl"
    }

    override fun getTransactionLogs(ppid: String): Flow<Resource<List<TransactionLog>>> {
        Log.d(TAG, "API CALL: GET /trx/ppid/$ppid")

        return safeApiFlowWithMapping(
            apiCall = {
                validatePpid(ppid)
                api.getTransactionLogs(ppid)
            },
            mapper = { response ->
                response.toTransactionLog()
            }
        )
    }

    override fun getTransactionSummary(ppid: String): Flow<Resource<TransactionSummary>> {
        return flow {
            emit(Resource.Loading())

            try {
                validatePpid(ppid)
                val transactionLogs = api.getTransactionLogs(ppid)
                val domainLogs = transactionLogs.map { it.toTransactionLog() }

                val incoming = domainLogs.filter { it.isIncomingTransaction() }
                val outgoing = domainLogs.filter { it.isOutgoingTransaction() }

                val summary = TransactionSummary(
                    totalCount = domainLogs.size,
                    incomingCount = incoming.size,
                    outgoingCount = outgoing.size,
                    totalIncoming = incoming.sumOf { it.tldAmount },
                    totalOutgoing = outgoing.sumOf { kotlin.math.abs(it.tldAmount) },
                    netAmount = domainLogs.sumOf { it.tldAmount },
                    latestBalance = domainLogs.firstOrNull()?.tldBalance ?: 0L
                )

                emit(Resource.Success(summary))
                Log.d(TAG, "Transaction summary calculated: ${summary.totalCount} transactions")

            } catch (e: Exception) {
                val appException = exceptionMapper.mapToAppException(e)
                emit(Resource.Error(appException))
                Log.e(TAG, "Transaction summary error", e)
            }
        }
    }

    private fun validatePpid(ppid: String) {
        if (ppid.isBlank()) {
            throw com.proyek.maganggsp.util.exceptions.AppException.ValidationException("PPID tidak boleh kosong")
        }
        if (ppid.length < 5) {
            throw com.proyek.maganggsp.util.exceptions.AppException.ValidationException("PPID harus minimal 5 karakter")
        }
        Log.d(TAG, "PPID validation passed: $ppid")
    }
}
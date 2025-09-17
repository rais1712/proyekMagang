// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/ProfileRepositoryImpl.kt - STREAMLINED
package com.proyek.maganggsp.data.repositoryImpl

import android.util.Log
import com.proyek.maganggsp.data.api.ProfileApi
import com.proyek.maganggsp.data.dto.*
import com.proyek.maganggsp.data.source.local.LoketHistoryManager
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.repository.ProfileRepository
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi,
    private val historyManager: LoketHistoryManager,
    exceptionMapper: ExceptionMapper
) : BaseRepository(exceptionMapper), ProfileRepository {

    companion object {
        private const val TAG = "ProfileRepositoryImpl"
    }

    /**
     * PRIMARY: Get profile data
     */
    override fun getProfile(ppid: String): Flow<Resource<Receipt>> {
        Log.d(TAG, "🌐 API CALL: GET /profiles/ppid/$ppid")

        return safeApiFlowWithItemMapping(
            apiCall = {
                validatePpid(ppid)
                api.getProfile(ppid)
            },
            mapper = { response ->
                val receipt = response.toReceipt()

                // Save to history for recent access
                try {
                    saveToHistory(receipt)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to save to history", e)
                }

                receipt
            }
        )
    }

    /**
     * PRIMARY: Get transaction logs
     */
    override fun getTransactionLogs(ppid: String): Flow<Resource<List<TransactionLog>>> {
        Log.d(TAG, "🌐 API CALL: GET /trx/ppid/$ppid")

        return safeApiFlowWithMapping(
            apiCall = {
                validatePpid(ppid)
                api.getTransactions(ppid)
            },
            mapper = { response ->
                response.toTransactionLog()
            }
        )
    }

    /**
     * PRIMARY: Update profile (block/unblock)
     */
    override fun updateProfile(currentPpid: String, newPpid: String): Flow<Resource<Unit>> {
        Log.d(TAG, "🔄 UPDATE PROFILE: $currentPpid -> $newPpid")

        return safeApiFlowUnit {
            validatePpid(currentPpid)
            validatePpid(newPpid)

            val updateRequest = UpdateProfileRequest(mpPpid = newPpid)
            Log.d(TAG, "📤 Update request: $updateRequest")

            api.updateProfile(currentPpid, updateRequest)
        }
    }

    /**
     * SEARCH: Find profiles by PPID
     */
    override fun searchProfiles(ppidQuery: String): Flow<Resource<List<Receipt>>> {
        Log.d(TAG, "🔍 SEARCH by PPID pattern: $ppidQuery")

        return safeApiFlow {
            // First try local cache
            val localResults = searchLocalCache(ppidQuery)

            if (localResults.isNotEmpty()) {
                Log.d(TAG, "📋 Local cache results: ${localResults.size}")
                return@safeApiFlow localResults
            }

            // If no local results and query looks like exact PPID, try direct API access
            if (isExactPpidFormat(ppidQuery)) {
                Log.d(TAG, "🎯 Trying direct API access for exact PPID: $ppidQuery")

                try {
                    val profileResponse = api.getProfile(ppidQuery)
                    val receipt = profileResponse.toReceipt()

                    // Save to history
                    saveToHistory(receipt)

                    listOf(receipt)
                } catch (apiError: Exception) {
                    Log.w(TAG, "Direct API access failed, returning empty")
                    emptyList()
                }
            } else {
                Log.d(TAG, "🔍 No results found for PPID pattern: $ppidQuery")
                emptyList()
            }
        }
    }

    // HELPER METHODS

    private fun validatePpid(ppid: String) {
        if (ppid.isBlank()) {
            throw com.proyek.maganggsp.util.exceptions.AppException.ValidationException("PPID tidak boleh kosong")
        }
        if (ppid.length < 5) {
            throw com.proyek.maganggsp.util.exceptions.AppException.ValidationException("PPID harus minimal 5 karakter")
        }
    }

    private fun searchLocalCache(query: String): List<Receipt> {
        return try {
            val histories = historyManager.searchByPpid(query)
            histories.map { history ->
                Receipt(
                    refNumber = "HISTORY-${history.ppid}",
                    idPelanggan = history.ppid,
                    amount = 0L,
                    logged = history.getFormattedTanggalAkses(),
                    ppid = history.ppid,
                    namaLoket = history.namaLoket,
                    nomorHP = history.nomorHP,
                    email = history.email ?: "",
                    alamat = history.alamat ?: ""
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Local cache search error", e)
            emptyList()
        }
    }

    private fun isExactPpidFormat(query: String): Boolean {
        val ppidPatterns = listOf(
            "^PIDLKTD\\d+.*$".toRegex(),
            "^[A-Z]{3,}[0-9]+.*$".toRegex()
        )
        return ppidPatterns.any { it.matches(query) }
    }

    private fun saveToHistory(receipt: Receipt) {
        try {
            val loket = com.proyek.maganggsp.domain.model.Loket(
                ppid = receipt.ppid,
                namaLoket = receipt.namaLoket,
                nomorHP = receipt.nomorHP,
                alamat = receipt.alamat,
                email = receipt.email,
                status = com.proyek.maganggsp.domain.model.LoketStatus.fromPpid(receipt.ppid),
                tanggalAkses = receipt.logged
            )
            historyManager.saveToHistory(loket)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to save to history", e)
        }
    }
}
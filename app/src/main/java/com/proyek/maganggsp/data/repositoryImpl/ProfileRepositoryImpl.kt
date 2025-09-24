// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/ProfileRepositoryImpl.kt - UPDATED MODULAR
package com.proyek.maganggsp.data.repositoryImpl

import android.util.Log
import com.proyek.maganggsp.data.api.ProfileApi
import com.proyek.maganggsp.data.dto.ProfileResponse
import com.proyek.maganggsp.data.dto.UpdateProfileRequest
import com.proyek.maganggsp.data.dto.toReceipt
import com.proyek.maganggsp.data.source.local.LoketHistoryManager
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.repository.ProfileRepository
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.ValidationUtils
import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
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
     * PRIMARY: Get profile data for card display
     * Maps API response to Receipt domain model for HomeFragment cards
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

                // Save to history for recent access tracking
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
     * PRIMARY: Update profile (block/unblock operations)
     * Based on HTTP request: PUT /profiles/ppid/{ppid} with {"mpPpid": "newValue"}
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
     * SEARCH: Find profiles by PPID pattern for search functionality
     */
    override fun searchProfiles(ppidQuery: String): Flow<Resource<List<Receipt>>> {
        Log.d(TAG, "🔍 SEARCH by PPID pattern: $ppidQuery")

        return flow {
            emit(Resource.Loading())

            try {
                // First try local cache for search results
                val localResults = searchLocalCache(ppidQuery)

                if (localResults.isNotEmpty()) {
                    Log.d(TAG, "📋 Local cache results: ${localResults.size}")
                    emit(Resource.Success(localResults))
                    return@flow
                }

                // If no local results and query looks like exact PPID, try direct API access
                if (isExactPpidFormat(ppidQuery)) {
                    Log.d(TAG, "🎯 Trying direct API access for exact PPID: $ppidQuery")

                    try {
                        val profileResponse = api.getProfile(ppidQuery)
                        val receipt = profileResponse.toReceipt()

                        // Save to history
                        saveToHistory(receipt)

                        emit(Resource.Success(listOf(receipt)))
                    } catch (apiError: Exception) {
                        Log.w(TAG, "Direct API access failed, returning empty")
                        emit(Resource.Empty)
                    }
                } else {
                    Log.d(TAG, "🔍 No results found for PPID pattern: $ppidQuery")
                    emit(Resource.Empty)
                }

            } catch (e: Exception) {
                val appException = exceptionMapper.mapToAppException(e)
                emit(Resource.Error(appException))
                Log.e(TAG, "❌ Search error", e)
            }
        }
    }

    /**
     * RECENT: Get recent profiles from history for HomeFragment display
     */
    override fun getRecentProfiles(): Flow<Resource<List<Receipt>>> {
        return flow {
            emit(Resource.Loading())

            try {
                val histories = historyManager.getRecentHistory()
                val receipts = histories.map { history ->
                    Receipt(
                        refNumber = "HISTORY-${history.ppid}",
                        idPelanggan = history.ppid,
                        amount = 0L, // No amount for profile cards
                        logged = getCurrentTimestamp(),
                        ppid = history.ppid,
                        namaLoket = history.namaLoket,
                        nomorHP = history.nomorHP,
                        email = history.email ?: "",
                        alamat = history.alamat ?: ""
                    )
                }

                Log.d(TAG, "📋 Recent profiles loaded: ${receipts.size}")
                emit(Resource.Success(receipts))

            } catch (e: Exception) {
                val appException = exceptionMapper.mapToAppException(e)
                emit(Resource.Error(appException))
                Log.e(TAG, "❌ Recent profiles error", e)
            }
        }
    }

    // HELPER METHODS

    private fun validatePpid(ppid: String) {
        val validation = ValidationUtils.validatePpidFormat(ppid)
        if (!validation.isValid) {
            throw com.proyek.maganggsp.util.exceptions.AppException.ValidationException(validation.message)
        }
    }

    private fun searchLocalCache(query: String): List<Receipt> {
        return try {
            val histories = historyManager.searchByPpid(query)
            histories.map { history ->
                Receipt(
                    refNumber = "SEARCH-${history.ppid}",
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

    private fun getCurrentTimestamp(): String {
        val formatter = java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.getDefault())
        formatter.timeZone = java.util.TimeZone.getTimeZone("UTC")
        return formatter.format(java.util.Date())
    }
}
// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/LoketRepositoryImpl.kt - REAL API ALIGNED
package com.proyek.maganggsp.data.repositoryImpl

import android.util.Log
import com.proyek.maganggsp.data.api.ProfileApi
import com.proyek.maganggsp.data.api.*
import com.proyek.maganggsp.data.source.local.LoketHistoryManager
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.LoketStatus
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoketRepositoryImpl @Inject constructor(
    private val api: ProfileApi, // UNIFIED: Use ProfileApi yang align dengan real endpoints
    private val historyManager: LoketHistoryManager,
    exceptionMapper: ExceptionMapper
) : BaseRepository(exceptionMapper), LoketRepository {

    companion object {
        private const val TAG = "LoketRepositoryImpl"
    }

    /**
     * REAL IMPLEMENTATION: Get comprehensive loket profile
     * Maps to: GET /profiles/ppid/{ppid}
     */
    override fun getLoketProfile(ppid: String): Flow<Resource<Loket>> {
        Log.d(TAG, "🌐 API CALL: GET /profiles/ppid/$ppid")

        return safeApiFlowWithItemMapping(
            apiCall = {
                validatePpid(ppid)
                api.getProfile(ppid)
            },
            mapper = { response ->
                val loket = response.toLoketDomain()
                Log.d(TAG, "✅ Loket status determined: ${loket.status}")

                // Save to history when successfully accessed
                try {
                    historyManager.saveToHistory(loket)
                } catch (e: Exception) {
                    Log.w(TAG, "Failed to save to history", e)
                }

                loket
            }
        )
    }

    /**
     * REAL IMPLEMENTATION: Get transaction logs
     * Maps to: GET /trx/ppid/{ppid}
     */
    override fun getLoketTransactions(ppid: String): Flow<Resource<List<TransactionLog>>> {
        Log.d(TAG, "🌐 API CALL: GET /trx/ppid/$ppid")

        return safeApiFlowWithMapping(
            apiCall = {
                validatePpid(ppid)
                api.getTransactions(ppid)
            },
            mapper = { response ->
                response.toDomain()
            }
        )
    }

    /**
     * REAL IMPLEMENTATION: Block loket using profile update
     * Logic: Add "blok" suffix to ppid
     * Maps to: PUT /profiles/ppid/{ppid} with {"mpPpid": "ppidblok"}
     */
    override fun blockLoket(ppid: String): Flow<Resource<Unit>> {
        Log.d(TAG, "🔒 BLOCK LOKET: $ppid -> ${ppid}blok")
        Log.d(TAG, "🌐 API CALL: PUT /profiles/ppid/$ppid with body: {\"mpPpid\": \"${ppid}blok\"}")

        return flow {
            emit(Resource.Loading())

            try {
                validatePpid(ppid)

                // Create block request
                val blockRequest = createBlockRequest(ppid)
                Log.d(TAG, "📤 Block request: $blockRequest")

                // Make API call
                val response = api.updateProfile(ppid, blockRequest)

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Block successful: $ppid")

                    // Update local cache status
                    updateLocalCacheStatus(ppid, LoketStatus.BLOCKED)

                    emit(Resource.Success(Unit))
                } else {
                    Log.e(TAG, "❌ Block failed: HTTP ${response.code()}")
                    emit(Resource.Error(
                        AppException.ServerException(
                            response.code(),
                            "Gagal memblokir loket: ${response.message()}"
                        )
                    ))
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Block loket error", e)
                val appException = exceptionMapper.mapToAppException(e)
                emit(Resource.Error(appException))
            }
        }
    }

    /**
     * REAL IMPLEMENTATION: Unblock loket using profile update
     * Logic: Remove "blok" suffix from ppid
     * Maps to: PUT /profiles/ppid/{ppid} with {"mpPpid": "originalPpid"}
     */
    override fun unblockLoket(ppid: String): Flow<Resource<Unit>> {
        val originalPpid = ppid.removeSuffix("blok")
        Log.d(TAG, "🔓 UNBLOCK LOKET: $ppid -> $originalPpid")
        Log.d(TAG, "🌐 API CALL: PUT /profiles/ppid/$ppid with body: {\"mpPpid\": \"$originalPpid\"}")

        return flow {
            emit(Resource.Loading())

            try {
                validatePpid(ppid)

                // Create unblock request
                val unblockRequest = createUnblockRequest(ppid)
                Log.d(TAG, "📤 Unblock request: $unblockRequest")

                // Make API call
                val response = api.updateProfile(ppid, unblockRequest)

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Unblock successful: $ppid")

                    // Update local cache status
                    updateLocalCacheStatus(ppid, LoketStatus.NORMAL)

                    emit(Resource.Success(Unit))
                } else {
                    Log.e(TAG, "❌ Unblock failed: HTTP ${response.code()}")
                    emit(Resource.Error(
                        AppException.ServerException(
                            response.code(),
                            "Gagal membuka blokir loket: ${response.message()}"
                        )
                    ))
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Unblock loket error", e)
                val appException = exceptionMapper.mapToAppException(e)
                emit(Resource.Error(appException))
            }
        }
    }

    /**
     * UPDATE PROFILE: Generic profile update method
     */
    override fun updateLoketProfile(ppid: String, updatedLoket: Loket): Flow<Resource<Unit>> {
        Log.d(TAG, "🔄 UPDATE PROFILE: $ppid")

        return flow {
            emit(Resource.Loading())

            try {
                validatePpid(ppid)

                // Determine new PPID based on status
                val newPpid = when (updatedLoket.status) {
                    LoketStatus.BLOCKED -> {
                        if (updatedLoket.ppid.endsWith("blok")) {
                            updatedLoket.ppid
                        } else {
                            "${updatedLoket.ppid}blok"
                        }
                    }
                    LoketStatus.NORMAL -> {
                        updatedLoket.ppid.removeSuffix("blok")
                    }
                    else -> updatedLoket.ppid
                }

                val updateRequest = UpdateProfileRequest(mpPpid = newPpid)
                Log.d(TAG, "📤 Update request: $updateRequest")

                val response = api.updateProfile(ppid, updateRequest)

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Update successful")

                    // Update local cache
                    updateLocalCacheStatus(ppid, updatedLoket.status)

                    emit(Resource.Success(Unit))
                } else {
                    Log.e(TAG, "❌ Update failed: HTTP ${response.code()}")
                    emit(Resource.Error(
                        AppException.ServerException(
                            response.code(),
                            "Gagal mengupdate profil loket"
                        )
                    ))
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Update profile error", e)
                val appException = exceptionMapper.mapToAppException(e)
                emit(Resource.Error(appException))
            }
        }
    }

    /**
     * UPDATED: PPID Search - Local cache + potential direct API access
     */
    override fun searchLoket(ppidQuery: String): Flow<Resource<List<Loket>>> {
        Log.d(TAG, "🔍 SEARCH by PPID pattern: $ppidQuery")

        return flow {
            emit(Resource.Loading())

            try {
                // First: Search in local cache
                val localResults = historyManager.searchByPpid(ppidQuery)
                Log.d(TAG, "📋 Local cache results: ${localResults.size}")

                // If we have local results, return them
                if (localResults.isNotEmpty()) {
                    val lokets = localResults.map { it.toLoket() }
                    emit(Resource.Success(lokets))
                    return@flow
                }

                // If no local results and query looks like exact PPID, try direct API access
                if (isExactPpidFormat(ppidQuery)) {
                    Log.d(TAG, "🎯 Trying direct API access for exact PPID: $ppidQuery")

                    try {
                        val profileResponse = api.getProfile(ppidQuery)
                        val loket = profileResponse.toLoketDomain()

                        // Save to history
                        historyManager.saveToHistory(loket)

                        emit(Resource.Success(listOf(loket)))
                        Log.d(TAG, "✅ Direct API access successful")

                    } catch (apiError: Exception) {
                        Log.w(TAG, "Direct API access failed, no results found")
                        emit(Resource.Empty)
                    }
                } else {
                    // Pattern search in cache didn't return results
                    Log.d(TAG, "🔍 No results found for PPID pattern: $ppidQuery")
                    emit(Resource.Empty)
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Search error", e)
                emit(Resource.Error(AppException.UnknownException("Pencarian gagal")))
            }
        }
    }

    /**
     * ACCESS BY PPID: Direct loket access (same as getLoketProfile)
     */
    override fun accessLoketByPpid(ppid: String): Flow<Resource<Loket>> {
        Log.d(TAG, "🎯 ACCESS by PPID: $ppid")
        return getLoketProfile(ppid) // Delegate to getLoketProfile
    }

    /**
     * RECENT LOKETS: From local history
     */
    override fun getRecentLokets(): Flow<Resource<List<Loket>>> {
        return flow {
            emit(Resource.Loading())

            try {
                val histories = historyManager.getRecentHistory()
                val lokets = histories.map { history ->
                    Loket(
                        ppid = history.ppid,
                        namaLoket = history.namaLoket,
                        nomorHP = history.nomorHP,
                        alamat = history.alamat ?: "",
                        email = history.email ?: "",
                        status = if (history.ppid.endsWith("blok")) LoketStatus.BLOCKED else LoketStatus.NORMAL,
                        tanggalAkses = history.getFormattedTanggalAkses()
                    )
                }

                Log.d(TAG, "📋 Recent lokets: ${lokets.size}")
                emit(Resource.Success(lokets))

            } catch (e: Exception) {
                Log.e(TAG, "❌ Get recent lokets error", e)
                emit(Resource.Error(AppException.UnknownException("Gagal memuat riwayat")))
            }
        }
    }

    /**
     * SAVE TO HISTORY: Local storage
     */
    override suspend fun saveToHistory(loket: Loket) {
        try {
            historyManager.saveToHistory(loket)
            Log.d(TAG, "💾 Saved to history: ${loket.ppid}")
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to save to history", e)
        }
    }

    /**
     * FAVORITES: Local storage management
     */
    override fun getFavoriteLokets(): Flow<Resource<List<Loket>>> {
        return flow {
            emit(Resource.Loading())

            try {
                val favoriteLokets = historyManager.getFavoriteLokets()
                Log.d(TAG, "⭐ Favorite lokets: ${favoriteLokets.size}")
                emit(Resource.Success(favoriteLokets))

            } catch (e: Exception) {
                Log.e(TAG, "❌ Get favorite lokets error", e)
                emit(Resource.Error(AppException.UnknownException("Gagal memuat favorit")))
            }
        }
    }

    override suspend fun toggleFavorite(ppid: String, isFavorite: Boolean) {
        try {
            if (isFavorite) {
                historyManager.addToFavorites(ppid)
                Log.d(TAG, "⭐ Added to favorites: $ppid")
            } else {
                historyManager.removeFromFavorites(ppid)
                Log.d(TAG, "⭐ Removed from favorites: $ppid")
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Toggle favorite error", e)
            throw AppException.UnknownException("Gagal mengupdate favorit")
        }
    }

    /**
     * HELPER METHODS
     */

    private fun validatePpid(ppid: String) {
        if (ppid.isBlank()) {
            throw AppException.ValidationException("PPID tidak boleh kosong")
        }
        if (ppid.length < 5) {
            throw AppException.ValidationException("PPID harus minimal 5 karakter")
        }
        Log.d(TAG, "✅ PPID validation passed: $ppid")
    }

    private fun isExactPpidFormat(query: String): Boolean {
        // Check if query looks like exact PPID format
        val ppidPatterns = listOf(
            "^PIDLKTD\\d+.*$".toRegex(),
            "^[A-Z]{3,}[0-9]+.*$".toRegex()
        )
        return ppidPatterns.any { it.matches(query) }
    }

    private fun updateLocalCacheStatus(ppid: String, newStatus: LoketStatus) {
        try {
            // Update status in local cache if loket exists in history
            historyManager.updateLoketStatus(ppid, newStatus)
        } catch (e: Exception) {
            Log.w(TAG, "Failed to update local cache status", e)
        }
    }

    /**
     * DEBUG INFO
     */
    fun getDebugInfo(): String {
        return """
        LoketRepository Debug Info:
        - API Base URL: ${com.proyek.maganggsp.BuildConfig.BASE_URL}
        - Block Logic: Append/Remove "blok" suffix to PPID
        - Available Operations: profile, transactions, block, unblock
        - Search: PPID-based (local cache + direct API)
        - Real Endpoints: GET /profiles/ppid/{ppid}, GET /trx/ppid/{ppid}, PUT /profiles/ppid/{ppid}
        """.trimIndent()
    }
}
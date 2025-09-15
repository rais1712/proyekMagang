// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/LoketRepositoryImpl.kt - REAL API IMPLEMENTATION
package com.proyek.maganggsp.data.repositoryImpl

import android.util.Log
import com.proyek.maganggsp.data.api.LoketApi
import com.proyek.maganggsp.data.dto.*
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
    private val api: LoketApi,
    private val historyManager: LoketHistoryManager,
    exceptionMapper: ExceptionMapper
) : BaseRepository(exceptionMapper), LoketRepository {

    companion object {
        private const val TAG = "LoketRepositoryImpl"
    }

    /**
     * REAL IMPLEMENTATION: Get comprehensive loket profile
     */
    override fun getLoketProfile(ppid: String): Flow<Resource<Loket>> {
        Log.d(TAG, "🌐 API CALL: GET /profiles/ppid/$ppid")

        return safeApiFlowWithItemMapping(
            apiCall = {
                validatePpid(ppid)
                api.getLoketProfile(ppid)
            },
            mapper = { response ->
                val loket = response.toDomain()
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
     */
    override fun getLoketTransactions(ppid: String): Flow<Resource<List<TransactionLog>>> {
        Log.d(TAG, "🌐 API CALL: GET /trx/ppid/$ppid")

        return safeApiFlowWithMapping(
            apiCall = {
                validatePpid(ppid)
                api.getLoketTransactions(ppid)
            },
            mapper = { response ->
                response.toDomain()
            }
        )
    }

    /**
     * REAL IMPLEMENTATION: Block loket using profile update
     * Logic: Add "blok" suffix to ppid
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
                val response = api.updateLoketProfile(ppid, blockRequest)

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Block successful: $ppid")
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
                val response = api.updateLoketProfile(ppid, unblockRequest)

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Unblock successful: $ppid")
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

                val response = api.updateLoketProfile(ppid, updateRequest)

                if (response.isSuccessful) {
                    Log.d(TAG, "✅ Update successful")
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
     * SEARCH: Since no search endpoint exists, implement manual PPID access
     */
    override fun searchLoket(phoneNumber: String): Flow<Resource<List<Loket>>> {
        Log.d(TAG, "🔍 SEARCH by phone not supported by API - returning empty results")

        return flow {
            emit(Resource.Loading())

            // Since there's no search endpoint, return recent history that matches
            try {
                val recentLokets = historyManager.getRecentHistory()
                val matchingLokets = recentLokets.filter { history ->
                    history.nomorHP.contains(phoneNumber, ignoreCase = true)
                }.map { history ->
                    Loket(
                        ppid = history.ppid,
                        namaLoket = history.namaLoket,
                        nomorHP = history.nomorHP,
                        alamat = "",
                        email = "",
                        status = if (history.ppid.endsWith("blok")) LoketStatus.BLOCKED else LoketStatus.NORMAL
                    )
                }

                Log.d(TAG, "🔍 Found ${matchingLokets.size} matching lokets in history")
                if (matchingLokets.isEmpty()) {
                    emit(Resource.Empty)
                } else {
                    emit(Resource.Success(matchingLokets))
                }

            } catch (e: Exception) {
                Log.e(TAG, "❌ Search error", e)
                emit(Resource.Error(AppException.UnknownException("Pencarian gagal")))
            }
        }
    }

    /**
     * ACCESS BY PPID: Direct loket access
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
                        alamat = "",
                        email = "",
                        status = if (history.ppid.endsWith("blok")) LoketStatus.BLOCKED else LoketStatus.NORMAL
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
     * VALIDATION: PPID format checking
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

    /**
     * DEBUG: Get repository debug info
     */
    fun getDebugInfo(): String {
        return """
        LoketRepository Debug Info:
        - API Base URL: ${com.proyek.maganggsp.BuildConfig.BASE_URL}
        - Block Logic: Append/Remove "blok" suffix to PPID
        - Available Operations: profile, transactions, block, unblock
        - Search: Local history only (no API endpoint)
        """.trimIndent()
    }
}
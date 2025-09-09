// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/LoketRepositoryImpl.kt
package com.proyek.maganggsp.data.repositoryImpl

import android.util.Log
import com.proyek.maganggsp.data.api.LoketApi
import com.proyek.maganggsp.data.dto.toDomain
import com.proyek.maganggsp.data.dto.toUpdateRequest
import com.proyek.maganggsp.data.source.local.LoketHistoryManager
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.model.LoketSearchHistory
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

    override fun getLoketProfile(ppid: String): Flow<Resource<Loket>> {
        Log.d(TAG, "Getting loket profile for PPID: $ppid")

        return safeApiFlowWithItemMapping(
            apiCall = {
                validatePpid(ppid)
                api.getLoketProfile(ppid)
            },
            mapper = { response ->
                val loket = response.toDomain()
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

    override fun getLoketTransactions(ppid: String): Flow<Resource<List<TransactionLog>>> {
        Log.d(TAG, "Getting transactions for loket PPID: $ppid")

        return safeApiFlowWithMapping(
            apiCall = {
                validatePpid(ppid)
                api.getLoketTransactions(ppid)
            },
            mapper = { transactionResponse ->
                transactionResponse.toDomain()
            }
        )
    }

    override fun updateLoketProfile(ppid: String, updatedLoket: Loket): Flow<Resource<Unit>> {
        Log.d(TAG, "Updating loket profile for PPID: $ppid")

        return safeApiFlowUnit {
            validatePpid(ppid)
            val updateRequest = updatedLoket.toUpdateRequest()
            api.updateLoketProfile(ppid, updateRequest)
        }
    }

    override fun accessLoketByPpid(ppid: String): Flow<Resource<Loket>> {
        Log.d(TAG, "Accessing loket by PPID: $ppid")

        return flow {
            emit(Resource.Loading())

            try {
                // Validate PPID first
                validatePpid(ppid)

                // Try to get from API
                val response = api.getLoketProfile(ppid)
                val loket = response.toDomain()

                // Save to access history
                historyManager.saveToHistory(loket)

                emit(Resource.Success(loket))
                Log.d(TAG, "Successfully accessed loket: ${loket.namaLoket}")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to access loket by PPID", e)
                val appException = exceptionMapper.mapToAppException(e)
                emit(Resource.Error(appException))
            }
        }
    }

    override fun getRecentLokets(): Flow<Resource<List<LoketSearchHistory>>> {
        return flow {
            emit(Resource.Loading())

            try {
                val recentLokets = historyManager.getRecentHistory()
                emit(Resource.Success(recentLokets))
                Log.d(TAG, "Retrieved ${recentLokets.size} recent lokets from history")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to get recent lokets", e)
                emit(Resource.Error(AppException.UnknownException("Failed to load recent history")))
            }
        }
    }

    override suspend fun saveToHistory(loket: Loket) {
        try {
            historyManager.saveToHistory(loket)
            Log.d(TAG, "Saved loket to history: ${loket.ppid}")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to save loket to history", e)
            // Non-critical operation, don't throw
        }
    }

    override fun getFavoriteLokets(): Flow<Resource<List<Loket>>> {
        return flow {
            emit(Resource.Loading())

            try {
                val favorites = historyManager.getFavoriteLokets()
                emit(Resource.Success(favorites))
                Log.d(TAG, "Retrieved ${favorites.size} favorite lokets")

            } catch (e: Exception) {
                Log.e(TAG, "Failed to get favorite lokets", e)
                emit(Resource.Error(AppException.UnknownException("Failed to load favorites")))
            }
        }
    }

    override suspend fun toggleFavorite(ppid: String, isFavorite: Boolean) {
        try {
            if (isFavorite) {
                historyManager.addToFavorites(ppid)
                Log.d(TAG, "Added PPID $ppid to favorites")
            } else {
                historyManager.removeFromFavorites(ppid)
                Log.d(TAG, "Removed PPID $ppid from favorites")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to toggle favorite status", e)
            throw AppException.UnknownException("Failed to update favorites")
        }
    }

    // Helper validation methods
    private fun validatePpid(ppid: String) {
        if (ppid.isBlank()) {
            throw AppException.ValidationException("PPID tidak boleh kosong")
        }
        if (ppid.length < 5) {
            throw AppException.ValidationException("PPID harus minimal 5 karakter")
        }
        Log.d(TAG, "PPID validation passed: $ppid")
    }

    // Bulk operations for future enhancement
    fun getMultipleLoketProfiles(ppids: List<String>): Flow<Resource<List<Loket>>> {
        return flow {
            emit(Resource.Loading())

            try {
                val lokets = mutableListOf<Loket>()

                ppids.forEach { ppid ->
                    try {
                        validatePpid(ppid)
                        val response = api.getLoketProfile(ppid)
                        lokets.add(response.toDomain())
                        Log.d(TAG, "Loaded loket profile: $ppid")
                    } catch (e: Exception) {
                        Log.w(TAG, "Failed to load profile for $ppid: ${e.message}")
                        // Continue with other PPIDs
                    }
                }

                emit(Resource.Success(lokets))
                Log.d(TAG, "Bulk operation completed: ${lokets.size}/${ppids.size} profiles loaded")

            } catch (e: Exception) {
                Log.e(TAG, "Bulk operation error", e)
                val appException = exceptionMapper.mapToAppException(e)
                emit(Resource.Error(appException))
            }
        }
    }

    // Search functionality for manual PPID entry with suggestions
    fun searchLoketSuggestions(query: String): Flow<Resource<List<LoketSearchHistory>>> {
        return flow {
            emit(Resource.Loading())

            try {
                val allHistory = historyManager.getRecentHistory()
                val filtered = allHistory.filter { history ->
                    history.ppid.contains(query, ignoreCase = true) ||
                            history.namaLoket.contains(query, ignoreCase = true) ||
                            history.nomorHP.contains(query, ignoreCase = true)
                }

                emit(Resource.Success(filtered))
                Log.d(TAG, "Search suggestions found: ${filtered.size} results for '$query'")

            } catch (e: Exception) {
                Log.e(TAG, "Search suggestions error", e)
                emit(Resource.Error(AppException.UnknownException("Failed to search history")))
            }
        }
    }
}
// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/ProfileRepositoryImpl.kt - ENHANCED
package com.proyek.maganggsp.data.repositoryImpl

import android.util.Log
import com.proyek.maganggsp.data.api.ProfileApi
import com.proyek.maganggsp.data.dto.UpdateProfileRequest
import com.proyek.maganggsp.data.dto.toDomain
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.repository.ProfileRepository
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ProfileRepositoryImpl @Inject constructor(
    private val api: ProfileApi,
    exceptionMapper: ExceptionMapper
) : BaseRepository(exceptionMapper), ProfileRepository {

    companion object {
        private const val TAG = "ProfileRepositoryImpl"
    }

    override fun getProfile(ppid: String): Flow<Resource<Receipt>> {
        Log.d(TAG, "🌐 API CALL: GET /profiles/ppid/$ppid")

        return safeApiFlowWithItemMapping(
            apiCall = {
                Log.d(TAG, "📡 Making API call to get profile for PPID: $ppid")
                validatePpid(ppid)
                api.getProfile(ppid)
            },
            mapper = { profileResponse ->
                Log.d(TAG, "🔄 Mapping ProfileResponse to Receipt domain model")
                profileResponse.toDomain()
            }
        )
    }

    override fun getTransactionLogs(ppid: String): Flow<Resource<List<TransactionLog>>> {
        Log.d(TAG, "🌐 API CALL: GET /trx/ppid/$ppid")

        return safeApiFlowWithMapping(
            apiCall = {
                Log.d(TAG, "📡 Making API call to get transaction logs for PPID: $ppid")
                validatePpid(ppid)
                api.getTransactions(ppid)
            },
            mapper = { transactionResponse ->
                Log.d(TAG, "🔄 Mapping TransactionResponse to TransactionLog domain model")
                transactionResponse.toDomain()
            }
        )
    }

    override fun updateProfile(ppid: String, newPpid: String): Flow<Resource<Unit>> {
        Log.d(TAG, "🌐 API CALL: PUT /profiles/ppid/$ppid with body: {\"mpPpid\": \"$newPpid\"}")

        return safeApiFlowUnit {
            Log.d(TAG, "📡 Making API call to update profile from $ppid to $newPpid")
            validatePpid(ppid)
            validatePpid(newPpid)
            api.updateProfile(ppid, UpdateProfileRequest(newPpid))
        }
    }

    // ✅ PHASE 1 FIX: Enhanced search implementation with proper error handling
    override fun searchProfiles(query: String): Flow<Resource<List<Receipt>>> {
        Log.d(TAG, "🔍 Search profiles with query: '$query'")

        return flow {
            emit(Resource.Loading())

            try {
                // Input validation
                when {
                    query.isBlank() -> {
                        Log.d(TAG, "📋 Empty query - returning empty results")
                        emit(Resource.Success(emptyList()))
                        return@flow
                    }
                    query.length < 3 -> {
                        Log.d(TAG, "⚠️ Query too short: ${query.length} chars")
                        emit(Resource.Success(emptyList()))
                        return@flow
                    }
                    else -> {
                        // For now, implement basic search by trying to get profile with query as PPID
                        // This is a temporary implementation until dedicated search endpoint is available
                        Log.d(TAG, "🔍 Attempting profile lookup with query as PPID: $query")

                        try {
                            val profileResponse = api.getProfile(query)
                            val receipt = profileResponse.toDomain()
                            emit(Resource.Success(listOf(receipt)))
                            Log.d(TAG, "✅ Search found profile: ${receipt.refNumber}")
                        } catch (e: Exception) {
                            // If direct lookup fails, return empty results (not an error for search)
                            Log.d(TAG, "🔍 No results found for query: $query")
                            emit(Resource.Success(emptyList()))
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "❌ Search error", e)
                val appException = exceptionMapper.mapToAppException(e)
                emit(Resource.Error(appException))
            }
        }
    }

    // ✅ PHASE 1 FIX: Add validation helper
    private fun validatePpid(ppid: String) {
        if (ppid.isBlank()) {
            throw AppException.ValidationException("PPID cannot be empty")
        }
        if (ppid.length < 5) {
            throw AppException.ValidationException("PPID must be at least 5 characters")
        }
        // Add more PPID format validation if needed
        Log.d(TAG, "✅ PPID validation passed: $ppid")
    }

    // ✅ PHASE 1 FIX: Add bulk operations for future use
    fun getMultipleProfiles(ppids: List<String>): Flow<Resource<List<Receipt>>> {
        Log.d(TAG, "🌐 BULK API CALL: GET multiple profiles for ${ppids.size} PPIDs")

        return flow {
            emit(Resource.Loading())

            try {
                val receipts = mutableListOf<Receipt>()

                ppids.forEach { ppid ->
                    try {
                        validatePpid(ppid)
                        val profileResponse = api.getProfile(ppid)
                        receipts.add(profileResponse.toDomain())
                        Log.d(TAG, "✅ Loaded profile: $ppid")
                    } catch (e: Exception) {
                        Log.w(TAG, "⚠️ Failed to load profile: $ppid - ${e.message}")
                        // Continue with other profiles
                    }
                }

                emit(Resource.Success(receipts))
                Log.d(TAG, "📊 Bulk operation completed: ${receipts.size}/${ppids.size} profiles loaded")

            } catch (e: Exception) {
                Log.e(TAG, "❌ Bulk operation error", e)
                val appException = exceptionMapper.mapToAppException(e)
                emit(Resource.Error(appException))
            }
        }
    }

    // ✅ PHASE 1 FIX: Add debug helper
    fun getDebugInfo(): String {
        return """
        ProfileRepository Debug Info:
        - API Base URL: ${com.proyek.maganggsp.BuildConfig.BASE_URL}
        - Build Type: ${com.proyek.maganggsp.BuildConfig.BUILD_TYPE}
        - Available Operations: getProfile, getTransactionLogs, updateProfile, searchProfiles
        """.trimIndent()
    }
}
// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/ProfileRepositoryImpl.kt - BUG FIX
package com.proyek.maganggsp.data.repositoryImpl

import android.util.Log
import com.proyek.maganggsp.data.api.ProfileApi
import com.proyek.maganggsp.data.dto.UpdateProfileRequest
import com.proyek.maganggsp.data.dto.toDomain
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
            api.updateProfile(ppid, UpdateProfileRequest(newPpid))
        }
    }

    override fun searchProfiles(query: String): Flow<Resource<List<Receipt>>> {
        Log.d(TAG, "🔍 Search profiles with query: '$query'")

        // BUG FIX: For now, return empty results since search endpoint is not yet implemented
        // This prevents crashes when search is triggered
        return safeApiFlow {
            Log.d(TAG, "📋 Search not yet implemented - returning empty results")
            emptyList<Receipt>()
        }
    }
}
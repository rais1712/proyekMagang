// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/ProfileRepositoryImpl.kt
package com.proyek.maganggsp.data.repositoryImpl

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

    override fun getProfile(ppid: String): Flow<Resource<Receipt>> {
        return safeApiFlowWithItemMapping(
            apiCall = { api.getProfile(ppid) },
            mapper = { it.toDomain() }
        )
    }

    override fun getTransactionLogs(ppid: String): Flow<Resource<List<TransactionLog>>> {
        return safeApiFlowWithMapping(
            apiCall = { api.getTransactions(ppid) },
            mapper = { it.toDomain() }
        )
    }

    override fun updateProfile(ppid: String, newPpid: String): Flow<Resource<Unit>> {
        return safeApiFlowUnit {
            api.updateProfile(ppid, UpdateProfileRequest(newPpid))
        }
    }

    override fun searchProfiles(query: String): Flow<Resource<List<Receipt>>> {
        // For now, implement as empty search - will be enhanced later
        // Could be implemented using profile endpoint with query parameters
        return safeApiFlow { emptyList<Receipt>() }
    }
}
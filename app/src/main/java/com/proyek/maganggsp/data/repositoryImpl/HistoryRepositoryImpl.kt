// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/HistoryRepositoryImpl.kt
package com.proyek.maganggsp.data.repositoryImpl

import com.proyek.maganggsp.data.api.HistoryApi
import com.proyek.maganggsp.data.dto.toDomain
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.HistoryRepository
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val api: HistoryApi,
    private val exceptionMapper: ExceptionMapper
) : BaseRepository(exceptionMapper), HistoryRepository {

    override fun getRecentHistory(): Flow<Resource<List<Loket>>> {
        return safeApiFlowWithMapping(
            apiCall = { api.getRecentHistory() },
            mapper = { it.toDomain() }
        )
    }

    override fun getFullHistory(): Flow<Resource<List<Loket>>> {
        return safeApiFlowWithMapping(
            apiCall = { api.getFullHistory() },
            mapper = { it.toDomain() }
        )
    }
}
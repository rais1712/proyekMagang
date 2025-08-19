// File: app/src/main/java/com/proyek/maganggsp/data/repositoryImpl/LoketRepositoryImpl.kt
package com.proyek.maganggsp.data.repositoryImpl

import com.proyek.maganggsp.data.api.LoketApi
import com.proyek.maganggsp.data.dto.toDomain
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.Mutasi
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.ExceptionMapper
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class LoketRepositoryImpl @Inject constructor(
    private val api: LoketApi,
    private val exceptionMapper: ExceptionMapper
) : BaseRepository(exceptionMapper), LoketRepository {

    override fun getLoketDetail(idLoket: String): Flow<Resource<Loket>> {
        return safeApiFlowWithItemMapping(
            apiCall = { api.getLoketDetail(idLoket) },
            mapper = { it.toDomain() }
        )
    }

    override fun getMutation(idLoket: String): Flow<Resource<List<Mutasi>>> {
        return safeApiFlowWithMapping(
            apiCall = { api.getMutation(idLoket) },
            mapper = { it.toDomain() }
        )
    }

    override fun searchLoket(query: String): Flow<Resource<List<Loket>>> {
        return safeApiFlowWithMapping(
            apiCall = { api.searchLoket(query) },
            mapper = { it.toDomain() }
        )
    }

    override fun blockLoket(idLoket: String): Flow<Resource<Unit>> {
        return safeApiFlowUnit { api.blockLoket(idLoket) }
    }

    override fun unblockLoket(idLoket: String): Flow<Resource<Unit>> {
        return safeApiFlowUnit { api.unblockLoket(idLoket) }
    }

    override fun flagMutation(idMutasi: String): Flow<Resource<Unit>> {
        return safeApiFlowUnit { api.flagMutation(idMutasi) }
    }

    override fun clearAllFlags(idLoket: String): Flow<Resource<Unit>> {
        return safeApiFlowUnit { api.clearAllFlags(idLoket) }
    }

    override fun getFlaggedLokets(): Flow<Resource<List<Loket>>> {
        return safeApiFlowWithMapping(
            apiCall = { api.getFlaggedLokets() },
            mapper = { it.toDomain() }
        )
    }

    override fun getBlockedLokets(): Flow<Resource<List<Loket>>> {
        return safeApiFlowWithMapping(
            apiCall = { api.getBlockedLokets() },
            mapper = { it.toDomain() }
        )
    }
}
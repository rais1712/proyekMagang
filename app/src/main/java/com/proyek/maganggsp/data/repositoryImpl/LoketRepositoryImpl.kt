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
    exceptionMapper: ExceptionMapper
) : BaseRepository(exceptionMapper), LoketRepository {

    override fun getLoketDetail(noLoket: String): Flow<Resource<Loket>> {
        return safeApiFlowWithItemMapping(
            apiCall = { api.getLoketDetail(noLoket) },
            mapper = { it.toDomain() }
        )
    }

    override fun getMutation(noLoket: String): Flow<Resource<List<Mutasi>>> {
        return safeApiFlowWithMapping(
            apiCall = { api.getMutation(noLoket) },
            mapper = { it.toDomain() }
        )
    }

    override fun searchLoket(query: String): Flow<Resource<List<Loket>>> {
        return safeApiFlowWithMapping(
            apiCall = { api.searchLoket(query) },
            mapper = { it.toDomain() }
        )
    }

    override fun blockLoket(noLoket: String): Flow<Resource<Unit>> {
        return safeApiFlowUnit { api.blockLoket(noLoket) }
    }

    override fun unblockLoket(noLoket: String): Flow<Resource<Unit>> {
        return safeApiFlowUnit { api.unblockLoket(noLoket) }
    }

    override fun flagMutation(idMutasi: String): Flow<Resource<Unit>> {
        return safeApiFlowUnit { api.flagMutation(idMutasi) }
    }

    override fun clearAllFlags(noLoket: String): Flow<Resource<Unit>> {
        return safeApiFlowUnit { api.clearAllFlags(noLoket) }
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
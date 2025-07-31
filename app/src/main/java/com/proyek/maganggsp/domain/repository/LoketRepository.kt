package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.Mutasi
import com.proyek.maganggsp.util.NetworkResult
import kotlinx.coroutines.flow.Flow

/**
 * Interface repository untuk operasi terkait Loket
 */
interface LoketRepository {
    suspend fun getLoketByPhone(phoneNumber: String): NetworkResult<Loket>

    suspend fun getMutations(loketNumber: String): NetworkResult<List<Mutasi>>

    suspend fun blockLoket(loketNumber: String): NetworkResult<Unit>

    suspend fun unblockLoket(loketNumber: String): NetworkResult<Unit>

    suspend fun getFlaggedLokets(): NetworkResult<List<Loket>>

    suspend fun getBlockedLokets(): NetworkResult<List<Loket>>

    fun getRecentHistory(): Flow<NetworkResult<List<Loket>>>

    fun getFullHistory(): Flow<NetworkResult<List<Loket>>>

    suspend fun flagTransaction(mutationId: String): NetworkResult<Unit>
}

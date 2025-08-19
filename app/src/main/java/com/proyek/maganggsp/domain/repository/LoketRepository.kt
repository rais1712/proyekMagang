package com.proyek.maganggsp.domain.repository

import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.Mutasi
import com.proyek.maganggsp.util.Resource
import kotlinx.coroutines.flow.Flow

// Interface ini adalah "kontrak" untuk lapisan data
interface LoketRepository {

    // SEMUA FUNGSI DI BAWAH INI DIPERBARUI
    // dari `suspend fun ...(): T` menjadi `fun ...(): Flow<Resource<T>>`

    fun getLoketDetail(noLoket: String): Flow<Resource<Loket>>

    fun getMutation(noLoket: String): Flow<Resource<List<Mutasi>>>

    fun searchLoket(query: String): Flow<Resource<List<Loket>>>

    fun blockLoket(noLoket: String): Flow<Resource<Unit>>

    fun unblockLoket(noLoket: String): Flow<Resource<Unit>>

    fun flagMutation(idMutasi: String): Flow<Resource<Unit>>

    fun clearAllFlags(noLoket: String): Flow<Resource<Unit>>

    fun getFlaggedLokets(): Flow<Resource<List<Loket>>>

    fun getBlockedLokets(): Flow<Resource<List<Loket>>>
}
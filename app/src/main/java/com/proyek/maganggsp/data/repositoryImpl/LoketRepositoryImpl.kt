package com.proyek.maganggsp.data.repositoryImpl

import com.proyek.maganggsp.data.api.LoketApi
import com.proyek.maganggsp.data.dto.toDomain
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.Mutasi
import com.proyek.maganggsp.domain.repository.LoketRepository
import javax.inject.Inject

class LoketRepositoryImpl @Inject constructor(
    private val api: LoketApi
) : LoketRepository {

    override suspend fun getLoketDetails(phoneNumber: String): Loket {
        return try {
            api.getLoketDetails(phoneNumber).toDomain()
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getMutations(loketId: String): List<Mutasi> {
        return try {
            api.getMutations(loketId).map { it.toDomain() }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun blockLoket(loketId: String) {
        try {
            api.blockLoket(loketId)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun unblockLoket(loketId: String) {
        try {
            api.unblockLoket(loketId)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun flagMutation(mutationId: String) {
        try {
            api.flagMutation(mutationId)
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getFlaggedLokets(): List<Loket> {
        return try {
            api.getFlaggedLokets().map { it.toDomain() }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getBlockedLokets(): List<Loket> {
        return try {
            api.getBlockedLokets().map { it.toDomain() }
        } catch (e: Exception) {
            throw e
        }

    }

    override suspend fun searchLoket(query: String): List<Loket> { // <<< TAMBAHKAN FUNGSI INI
        return try {
            api.searchLoket(query).map { it.toDomain() }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun clearAllFlags(loketId: String) { // <<< TAMBAHKAN FUNGSI INI
        try {
            api.clearAllFlags(loketId)
        } catch (e: Exception) {
            throw e
        }
    }

}
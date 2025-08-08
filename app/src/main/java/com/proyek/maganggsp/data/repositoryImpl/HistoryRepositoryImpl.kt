package com.proyek.maganggsp.data.repositoryImpl

import com.proyek.maganggsp.data.api.HistoryApi
import com.proyek.maganggsp.data.dto.toDomain
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.repository.HistoryRepository
import javax.inject.Inject

class HistoryRepositoryImpl @Inject constructor(
    private val api: HistoryApi
) : HistoryRepository {

    override suspend fun getRecentHistory(): List<Loket> {
        return try {
            api.getRecentHistory().map { it.toDomain() }
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun getFullHistory(): List<Loket> {
        return try {
            api.getFullHistory().map { it.toDomain() }
        } catch (e: Exception) {
            throw e
        }
    }
}
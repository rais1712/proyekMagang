// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/SearchLoketHistoryUseCase.kt
package com.proyek.maganggsp.domain.usecase.loket

import com.proyek.maganggsp.domain.model.LoketSearchHistory
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Use case for searching through loket history with suggestions
 */
class SearchLoketHistoryUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    operator fun invoke(query: String): Flow<Resource<List<LoketSearchHistory>>> = flow {
        try {
            when {
                query.isBlank() -> {
                    // Return recent history when query is empty
                    repository.getRecentLokets().collect { emit(it) }
                }
                query.length < 2 -> {
                    emit(Resource.Success(emptyList()))
                }
                else -> {
                    // Use repository search functionality
                    if (repository is com.proyek.maganggsp.data.repositoryImpl.LoketRepositoryImpl) {
                        repository.searchLoketSuggestions(query).collect { emit(it) }
                    } else {
                        emit(Resource.Success(emptyList()))
                    }
                }
            }
        } catch (e: Exception) {
            emit(Resource.Error(AppException.UnknownException("Search failed: ${e.message}")))
        }
    }
}
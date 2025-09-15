// File: app/src/main/java/com/proyek/maganggsp/domain/usecase/loket/LoketUseCases.kt - SIMPLIFIED & REAL
package com.proyek.maganggsp.domain.usecase.loket

import android.util.Log
import com.proyek.maganggsp.domain.model.*
import com.proyek.maganggsp.domain.repository.LoketRepository
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * REAL API: Get Loket Profile Use Case
 * Maps to: GET /profiles/ppid/{ppid}
 */
class GetLoketProfileUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    companion object {
        private const val TAG = "GetLoketProfileUseCase"
    }

    operator fun invoke(ppid: String): Flow<Resource<Loket>> = flow {
        try {
            Log.d(TAG, "🎯 Get loket profile for: $ppid")

            // Validate PPID
            if (!ModelValidation.isValidPpid(ppid)) {
                emit(Resource.Error(AppException.ValidationException("Format PPID tidak valid")))
                return@flow
            }

            // Delegate to repository
            repository.getLoketProfile(ppid).collect { resource ->
                emit(resource)

                when (resource) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Profile loaded: ${resource.data.namaLoket} (${resource.data.status})")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Profile load error: ${resource.exception.message}")
                    }
                    else -> Unit
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Use case error", e)
            emit(Resource.Error(AppException.UnknownException("Gagal memuat profil loket")))
        }
    }
}

/**
 * REAL API: Get Transaction Logs Use Case
 * Maps to: GET /trx/ppid/{ppid}
 */
class GetLoketTransactionsUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    companion object {
        private const val TAG = "GetLoketTransactionsUseCase"
    }

    operator fun invoke(ppid: String): Flow<Resource<List<TransactionLog>>> = flow {
        try {
            Log.d(TAG, "📊 Get transactions for: $ppid")

            // Validate PPID
            if (!ModelValidation.isValidPpid(ppid)) {
                emit(Resource.Error(AppException.ValidationException("Format PPID tidak valid")))
                return@flow
            }

            // Delegate to repository
            repository.getLoketTransactions(ppid).collect { resource ->
                emit(resource)

                when (resource) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Transactions loaded: ${resource.data.size} items")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Transactions load error: ${resource.exception.message}")
                    }
                    else -> Unit
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Use case error", e)
            emit(Resource.Error(AppException.UnknownException("Gagal memuat log transaksi")))
        }
    }
}

/**
 * REAL API: Block Loket Use Case
 * Implementation: PUT /profiles/ppid/{ppid} with {"mpPpid": "ppid + blok"}
 */
class BlockLoketUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    companion object {
        private const val TAG = "BlockLoketUseCase"
    }

    operator fun invoke(ppid: String): Flow<Resource<Unit>> = flow {
        try {
            Log.d(TAG, "🔒 Block loket: $ppid")

            // Validate PPID
            if (!ModelValidation.isValidPpid(ppid)) {
                emit(Resource.Error(AppException.ValidationException("Format PPID tidak valid")))
                return@flow
            }

            // Check if already blocked
            if (ppid.endsWith("blok")) {
                emit(Resource.Error(AppException.ValidationException("Loket sudah diblokir")))
                return@flow
            }

            // Delegate to repository
            repository.blockLoket(ppid).collect { resource ->
                emit(resource)

                when (resource) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Loket blocked successfully: $ppid")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Block error: ${resource.exception.message}")
                    }
                    else -> Unit
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Use case error", e)
            emit(Resource.Error(AppException.UnknownException("Gagal memblokir loket")))
        }
    }
}

/**
 * REAL API: Unblock Loket Use Case
 * Implementation: PUT /profiles/ppid/{ppid} with {"mpPpid": "ppid without blok"}
 */
class UnblockLoketUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    companion object {
        private const val TAG = "UnblockLoketUseCase"
    }

    operator fun invoke(ppid: String): Flow<Resource<Unit>> = flow {
        try {
            Log.d(TAG, "🔓 Unblock loket: $ppid")

            // Validate PPID
            if (!ModelValidation.isValidPpid(ppid)) {
                emit(Resource.Error(AppException.ValidationException("Format PPID tidak valid")))
                return@flow
            }

            // Check if not blocked
            if (!ppid.endsWith("blok")) {
                emit(Resource.Error(AppException.ValidationException("Loket tidak dalam status diblokir")))
                return@flow
            }

            // Delegate to repository
            repository.unblockLoket(ppid).collect { resource ->
                emit(resource)

                when (resource) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Loket unblocked successfully: $ppid")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Unblock error: ${resource.exception.message}")
                    }
                    else -> Unit
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Use case error", e)
            emit(Resource.Error(AppException.UnknownException("Gagal membuka blokir loket")))
        }
    }
}

/**
 * LOCAL: Search Loket Use Case
 * Since API doesn't have search endpoint, searches local history
 */
class SearchLoketUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    companion object {
        private const val TAG = "SearchLoketUseCase"
        private const val MIN_SEARCH_LENGTH = 3
    }

    operator fun invoke(query: String): Flow<Resource<List<Loket>>> = flow {
        try {
            Log.d(TAG, "🔍 Search loket with query: '$query'")

            // Validate query
            val cleanQuery = query.trim()
            if (cleanQuery.length < MIN_SEARCH_LENGTH) {
                Log.d(TAG, "⚠️ Query too short: ${cleanQuery.length} chars")
                emit(Resource.Success(emptyList()))
                return@flow
            }

            // Phone number validation for search
            val isPhoneQuery = cleanQuery.all { it.isDigit() || it == '+' }
            if (isPhoneQuery && cleanQuery.length < 10) {
                emit(Resource.Error(AppException.ValidationException("Nomor telepon minimal 10 digit")))
                return@flow
            }

            // Delegate to repository (searches local history)
            repository.searchLoket(cleanQuery).collect { resource ->
                emit(resource)

                when (resource) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Search results: ${resource.data.size} lokets")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Search error: ${resource.exception.message}")
                    }
                    is Resource.Empty -> {
                        Log.d(TAG, "🔍 No results found for: '$cleanQuery'")
                    }
                    else -> Unit
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Search use case error", e)
            emit(Resource.Error(AppException.UnknownException("Pencarian gagal")))
        }
    }

    /**
     * Quick validation for UI feedback
     */
    fun validateSearchQuery(query: String): ValidationResult {
        val cleaned = query.trim()

        return when {
            cleaned.isBlank() -> ValidationResult.Error("Query tidak boleh kosong")
            cleaned.length < MIN_SEARCH_LENGTH -> ValidationResult.Error("Minimal $MIN_SEARCH_LENGTH karakter")
            cleaned.all { it.isDigit() || it == '+' } && cleaned.length < 10 ->
                ValidationResult.Error("Nomor telepon minimal 10 digit")
            else -> ValidationResult.Success(cleaned)
        }
    }

    sealed class ValidationResult(val message: String) {
        data class Success(val query: String) : ValidationResult("Valid")
        data class Error(val errorMessage: String) : ValidationResult(errorMessage)

        val isError: Boolean get() = this is Error
        val isSuccess: Boolean get() = this is Success
    }
}

/**
 * LOCAL: Get Recent Lokets Use Case
 */
class GetRecentLoketsUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    companion object {
        private const val TAG = "GetRecentLoketsUseCase"
    }

    operator fun invoke(): Flow<Resource<List<Loket>>> = flow {
        try {
            Log.d(TAG, "📋 Get recent lokets")

            repository.getRecentLokets().collect { resource ->
                emit(resource)

                when (resource) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Recent lokets: ${resource.data.size} items")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Recent lokets error: ${resource.exception.message}")
                    }
                    else -> Unit
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Use case error", e)
            emit(Resource.Error(AppException.UnknownException("Gagal memuat riwayat loket")))
        }
    }
}

/**
 * CONVENIENCE: Access Loket by PPID Use Case
 * Direct access untuk known PPID (same as GetLoketProfile)
 */
class AccessLoketByPpidUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    companion object {
        private const val TAG = "AccessLoketByPpidUseCase"
    }

    operator fun invoke(ppid: String): Flow<Resource<Loket>> = flow {
        try {
            Log.d(TAG, "🎯 Access loket by PPID: $ppid")

            // Validate PPID format
            if (!ModelValidation.isValidPpid(ppid)) {
                emit(Resource.Error(AppException.ValidationException("Format PPID tidak valid")))
                return@flow
            }

            // Use repository access method
            repository.accessLoketByPpid(ppid).collect { resource ->
                emit(resource)

                when (resource) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Loket accessed: ${resource.data.namaLoket}")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Access error: ${resource.exception.message}")
                    }
                    else -> Unit
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Use case error", e)
            emit(Resource.Error(AppException.UnknownException("Gagal mengakses loket")))
        }
    }
}

/**
 * LOCAL: Get Favorite Lokets Use Case
 */
class GetFavoriteLoketsUseCase @Inject constructor(
    private val repository: LoketRepository
) {
    companion object {
        private const val TAG = "GetFavoriteLoketsUseCase"
    }

    operator fun invoke(): Flow<Resource<List<Loket>>> = flow {
        try {
            Log.d(TAG, "⭐ Get favorite lokets")

            repository.getFavoriteLokets().collect { resource ->
                emit(resource)

                when (resource) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Favorite lokets: ${resource.data.size} items")
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Favorite lokets error: ${resource.exception.message}")
                    }
                    else -> Unit
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Use case error", e)
            emit(Resource.Error(AppException.UnknownException("Gagal memuat favorit loket")))
        }
    }
}

/**
 * COMBINED: Comprehensive Loket Management Use Case
 * Single use case untuk complete loket operations
 */
class LoketManagementUseCase @Inject constructor(
    private val getProfileUseCase: GetLoketProfileUseCase,
    private val getTransactionsUseCase: GetLoketTransactionsUseCase,
    private val blockUseCase: BlockLoketUseCase,
    private val unblockUseCase: UnblockLoketUseCase
) {
    companion object {
        private const val TAG = "LoketManagementUseCase"
    }

    /**
     * COMPREHENSIVE: Get complete loket data (profile + transactions)
     */
    suspend fun getCompleteLoketData(ppid: String): Pair<Resource<Loket>, Resource<List<TransactionLog>>> {
        Log.d(TAG, "📦 Get complete data for: $ppid")

        val profileResult = Resource.Loading<Loket>()
        val transactionResult = Resource.Loading<List<TransactionLog>>()

        return Pair(profileResult, transactionResult)
    }

    /**
     * SMART: Toggle loket block status
     */
    fun toggleBlockStatus(ppid: String): Flow<Resource<Unit>> = flow {
        try {
            Log.d(TAG, "🔄 Toggle block status for: $ppid")

            if (ppid.endsWith("blok")) {
                // Currently blocked, unblock it
                unblockUseCase(ppid).collect { emit(it) }
            } else {
                // Currently normal, block it
                blockUseCase(ppid).collect { emit(it) }
            }

        } catch (e: Exception) {
            Log.e(TAG, "❌ Toggle block error", e)
            emit(Resource.Error(AppException.UnknownException("Gagal mengubah status blokir")))
        }
    }
}
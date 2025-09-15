// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/DetailLoketViewModel.kt - MVP CORE
package com.proyek.maganggsp.presentation.detail_loket

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.usecase.loket.GetLoketProfileUseCase
import com.proyek.maganggsp.domain.usecase.loket.GetLoketTransactionsUseCase
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MVP CORE: DetailLoketViewModel dengan block/unblock functionality
 */
@HiltViewModel
class DetailLoketViewModel @Inject constructor(
    private val getLoketProfileUseCase: GetLoketProfileUseCase,
    private val getLoketTransactionsUseCase: GetLoketTransactionsUseCase,
    private val loketRepository: com.proyek.maganggsp.domain.repository.LoketRepository
) : ViewModel() {

    companion object {
        private const val TAG = "DetailLoketViewModel"
    }

    // Loket profile state
    private val _loketProfile = MutableStateFlow<Resource<Loket>>(Resource.Empty)
    val loketProfile: StateFlow<Resource<Loket>> = _loketProfile.asStateFlow()

    // Transaction logs state
    private val _transactionLogs = MutableStateFlow<Resource<List<TransactionLog>>>(Resource.Empty)
    val transactionLogs: StateFlow<Resource<List<TransactionLog>>> = _transactionLogs.asStateFlow()

    // Block/Unblock action result
    private val _blockUnblockResult = MutableStateFlow<Resource<Unit>>(Resource.Empty)
    val blockUnblockResult: StateFlow<Resource<Unit>> = _blockUnblockResult.asStateFlow()

    /**
     * Load loket profile dengan comprehensive data
     */
    fun loadLoketProfile(ppid: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading loket profile for PPID: $ppid")

                getLoketProfileUseCase(ppid).collect { resource ->
                    _loketProfile.value = resource

                    when (resource) {
                        is Resource.Success -> {
                            Log.d(TAG, "Loket profile loaded: ${resource.data.namaLoket}")
                            AppUtils.logInfo(TAG, "Profile loaded for ${resource.data.namaLoket}")
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "Loket profile error: ${resource.exception.message}")
                            AppUtils.logError(TAG, "Profile load failed", resource.exception)
                        }
                        is Resource.Loading -> {
                            Log.d(TAG, "Loading loket profile...")
                        }
                        is Resource.Empty -> {
                            Log.w(TAG, "No loket profile data found for $ppid")
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Loket profile load error", e)
                _loketProfile.value = Resource.Error(
                    com.proyek.maganggsp.util.exceptions.AppException.UnknownException(
                        "Gagal memuat profil loket: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * Load transaction logs untuk specific loket
     */
    fun loadTransactionLogs(ppid: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading transaction logs for PPID: $ppid")

                getLoketTransactionsUseCase(ppid).collect { resource ->
                    _transactionLogs.value = resource

                    when (resource) {
                        is Resource.Success -> {
                            Log.d(TAG, "Transaction logs loaded: ${resource.data.size} transactions")
                            AppUtils.logInfo(TAG, "Loaded ${resource.data.size} transactions for $ppid")
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "Transaction logs error: ${resource.exception.message}")
                            AppUtils.logError(TAG, "Transaction logs load failed", resource.exception)
                        }
                        is Resource.Loading -> {
                            Log.d(TAG, "Loading transaction logs...")
                        }
                        is Resource.Empty -> {
                            Log.d(TAG, "No transaction logs found for $ppid")
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Transaction logs load error", e)
                _transactionLogs.value = Resource.Error(
                    com.proyek.maganggsp.util.exceptions.AppException.UnknownException(
                        "Gagal memuat log transaksi: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * Block loket dengan confirmation
     */
    fun blockLoket(ppid: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Blocking loket with PPID: $ppid")
                _blockUnblockResult.value = Resource.Loading()

                loketRepository.blockLoket(ppid).collect { resource ->
                    _blockUnblockResult.value = resource

                    when (resource) {
                        is Resource.Success -> {
                            Log.d(TAG, "Loket blocked successfully: $ppid")
                            AppUtils.logInfo(TAG, "Successfully blocked loket $ppid")
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "Block loket error: ${resource.exception.message}")
                            AppUtils.logError(TAG, "Block operation failed", resource.exception)
                        }
                        is Resource.Loading -> {
                            Log.d(TAG, "Blocking loket in progress...")
                        }
                        is Resource.Empty -> { /* Not applicable */ }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Block loket error", e)
                _blockUnblockResult.value = Resource.Error(
                    com.proyek.maganggsp.util.exceptions.AppException.UnknownException(
                        "Gagal memblokir loket: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * Unblock loket dengan confirmation
     */
    fun unblockLoket(ppid: String) {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Unblocking loket with PPID: $ppid")
                _blockUnblockResult.value = Resource.Loading()

                loketRepository.unblockLoket(ppid).collect { resource ->
                    _blockUnblockResult.value = resource

                    when (resource) {
                        is Resource.Success -> {
                            Log.d(TAG, "Loket unblocked successfully: $ppid")
                            AppUtils.logInfo(TAG, "Successfully unblocked loket $ppid")
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "Unblock loket error: ${resource.exception.message}")
                            AppUtils.logError(TAG, "Unblock operation failed", resource.exception)
                        }
                        is Resource.Loading -> {
                            Log.d(TAG, "Unblocking loket in progress...")
                        }
                        is Resource.Empty -> { /* Not applicable */ }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Unblock loket error", e)
                _blockUnblockResult.value = Resource.Error(
                    com.proyek.maganggsp.util.exceptions.AppException.UnknownException(
                        "Gagal membuka blokir loket: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * Refresh both profile and transaction data
     */
    fun refresh(ppid: String) {
        loadLoketProfile(ppid)
        // Transaction logs will be auto-loaded when profile loads successfully
    }

    /**
     * Clear action result state
     */
    fun clearActionResult() {
        _blockUnblockResult.value = Resource.Empty
    }

    /**
     * Get current loket data
     */
    fun getCurrentLoket(): Loket? {
        return (_loketProfile.value as? Resource.Success)?.data
    }

    /**
     * Get transaction statistics
     */
    fun getTransactionStats(): String? {
        val transactions = (_transactionLogs.value as? Resource.Success)?.data ?: return null

        val incoming = transactions.filter { it.isIncomingTransaction() }
        val outgoing = transactions.filter { it.isOutgoingTransaction() }
        val totalIncoming = incoming.sumOf { it.tldAmount }
        val totalOutgoing = outgoing.sumOf { kotlin.math.abs(it.tldAmount) }
        val latestBalance = transactions.firstOrNull()?.tldBalance ?: 0L

        return """
        Statistik Transaksi:
        - Total Transaksi: ${transactions.size}
        - Transaksi Masuk: ${incoming.size} (${AppUtils.formatCurrency(totalIncoming)})
        - Transaksi Keluar: ${outgoing.size} (${AppUtils.formatCurrency(totalOutgoing)})
        - Saldo Terakhir: ${AppUtils.formatCurrency(latestBalance)}
        """.trimIndent()
    }

    /**
     * Debug info
     */
    fun getDebugInfo(): String {
        val currentLoket = getCurrentLoket()
        return """
        DetailLoketViewModel Debug Info:
        - Current Loket: ${currentLoket?.namaLoket ?: "None"}
        - PPID: ${currentLoket?.ppid ?: "None"}
        - Status: ${currentLoket?.status ?: "None"}
        - Profile State: ${_loketProfile.value.javaClass.simpleName}
        - Transaction State: ${_transactionLogs.value.javaClass.simpleName}
        - Action State: ${_blockUnblockResult.value.javaClass.simpleName}
        """.trimIndent()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "ViewModel cleared")
    }
}
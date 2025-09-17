// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/DetailLoketViewModel.kt - STREAMLINED
package com.proyek.maganggsp.presentation.detail_loket

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.LoketProfile
import com.proyek.maganggsp.domain.usecase.profile.GetProfileUseCase
import com.proyek.maganggsp.domain.usecase.profile.GetTransactionLogsUseCase
import com.proyek.maganggsp.domain.usecase.profile.BlockUnblockUseCase
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * STREAMLINED: DetailLoketViewModel focused on profile info + transaction logs
 * Eliminates complex loket management, focuses on essential data display
 */
@HiltViewModel
class DetailLoketViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val getTransactionLogsUseCase: GetTransactionLogsUseCase,
    private val blockUnblockUseCase: BlockUnblockUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "DetailLoketViewModel"
    }

    // STREAMLINED STATE MANAGEMENT
    private val _profileState = MutableStateFlow<Resource<LoketProfile>>(Resource.Empty)
    val profileState: StateFlow<Resource<LoketProfile>> = _profileState.asStateFlow()

    private val _transactionLogs = MutableStateFlow<Resource<List<TransactionLog>>>(Resource.Empty)
    val transactionLogs: StateFlow<Resource<List<TransactionLog>>> = _transactionLogs.asStateFlow()

    private val _blockUnblockResult = MutableStateFlow<Resource<Unit>>(Resource.Empty)
    val blockUnblockResult: StateFlow<Resource<Unit>> = _blockUnblockResult.asStateFlow()

    /**
     * STREAMLINED: Load profile info dari Receipt API
     */
    fun loadProfile(ppid: String) {
        viewModelScope.launch {
            try {
                AppUtils.logInfo(TAG, "Loading profile for PPID: $ppid")

                getProfileUseCase(ppid).collect { resource ->
                    // Convert Receipt to LoketProfile for UI compatibility
                    _profileState.value = when (resource) {
                        is Resource.Success -> {
                            val receipt = resource.data
                            val loketProfile = LoketProfile(
                                ppid = receipt.ppid,
                                namaLoket = receipt.namaLoket.takeIf { it.isNotBlank() } ?: "Receipt ${receipt.refNumber}",
                                nomorHP = receipt.nomorHP,
                                alamat = receipt.alamat,
                                email = receipt.email,
                                status = com.proyek.maganggsp.domain.model.LoketStatus.fromPpid(receipt.ppid),
                                saldoTerakhir = receipt.saldoTerakhir,
                                tanggalAkses = receipt.tanggalAkses
                            )
                            Resource.Success(loketProfile)
                        }
                        is Resource.Error -> Resource.Error(resource.exception)
                        is Resource.Loading -> Resource.Loading()
                        is Resource.Empty -> Resource.Empty
                    }

                    when (resource) {
                        is Resource.Success -> {
                            AppUtils.logInfo(TAG, "Profile loaded successfully")
                            // Auto-load transaction logs after profile loads
                            loadTransactionLogs(ppid)
                        }
                        is Resource.Error -> {
                            AppUtils.logError(TAG, "Profile load error", resource.exception)
                        }
                        is Resource.Loading -> {
                            AppUtils.logDebug(TAG, "Loading profile...")
                        }
                        is Resource.Empty -> {
                            AppUtils.logDebug(TAG, "No profile data")
                        }
                    }
                }

            } catch (e: Exception) {
                AppUtils.logError(TAG, "Profile load exception", e)
                _profileState.value = Resource.Error(
                    com.proyek.maganggsp.util.exceptions.AppException.UnknownException(
                        "Gagal memuat profil: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * STREAMLINED: Load transaction logs
     */
    fun loadTransactionLogs(ppid: String) {
        viewModelScope.launch {
            try {
                AppUtils.logInfo(TAG, "Loading transaction logs for PPID: $ppid")

                getTransactionLogsUseCase(ppid).collect { resource ->
                    _transactionLogs.value = resource

                    when (resource) {
                        is Resource.Success -> {
                            AppUtils.logInfo(TAG, "Transaction logs loaded: ${resource.data.size} transactions")
                        }
                        is Resource.Error -> {
                            AppUtils.logError(TAG, "Transaction logs error", resource.exception)
                            // Create placeholder data for testing
                            createPlaceholderTransactionLogs(ppid)
                        }
                        is Resource.Loading -> {
                            AppUtils.logDebug(TAG, "Loading transaction logs...")
                        }
                        is Resource.Empty -> {
                            AppUtils.logDebug(TAG, "No transaction logs available")
                        }
                    }
                }

            } catch (e: Exception) {
                AppUtils.logError(TAG, "Transaction logs exception", e)
                createPlaceholderTransactionLogs(ppid)
            }
        }
    }

    /**
     * STREAMLINED: Block loket operation
     */
    fun blockLoket(ppid: String) {
        viewModelScope.launch {
            try {
                AppUtils.logInfo(TAG, "Blocking loket: $ppid")
                _blockUnblockResult.value = Resource.Loading()

                blockUnblockUseCase.blockLoket(ppid).collect { resource ->
                    _blockUnblockResult.value = resource

                    when (resource) {
                        is Resource.Success -> {
                            AppUtils.logInfo(TAG, "Loket blocked successfully")
                            // Reload profile to get updated status
                            loadProfile(ppid)
                        }
                        is Resource.Error -> {
                            AppUtils.logError(TAG, "Block operation failed", resource.exception)
                        }
                        is Resource.Loading -> {
                            AppUtils.logDebug(TAG, "Block operation in progress...")
                        }
                        is Resource.Empty -> { /* Not applicable */ }
                    }
                }

            } catch (e: Exception) {
                AppUtils.logError(TAG, "Block operation exception", e)
                _blockUnblockResult.value = Resource.Error(
                    com.proyek.maganggsp.util.exceptions.AppException.UnknownException(
                        "Gagal memblokir loket: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * STREAMLINED: Unblock loket operation
     */
    fun unblockLoket(ppid: String) {
        viewModelScope.launch {
            try {
                AppUtils.logInfo(TAG, "Unblocking loket: $ppid")
                _blockUnblockResult.value = Resource.Loading()

                blockUnblockUseCase.unblockLoket(ppid).collect { resource ->
                    _blockUnblockResult.value = resource

                    when (resource) {
                        is Resource.Success -> {
                            AppUtils.logInfo(TAG, "Loket unblocked successfully")
                            // Reload profile to get updated status
                            loadProfile(ppid)
                        }
                        is Resource.Error -> {
                            AppUtils.logError(TAG, "Unblock operation failed", resource.exception)
                        }
                        is Resource.Loading -> {
                            AppUtils.logDebug(TAG, "Unblock operation in progress...")
                        }
                        is Resource.Empty -> { /* Not applicable */ }
                    }
                }

            } catch (e: Exception) {
                AppUtils.logError(TAG, "Unblock operation exception", e)
                _blockUnblockResult.value = Resource.Error(
                    com.proyek.maganggsp.util.exceptions.AppException.UnknownException(
                        "Gagal membuka blokir loket: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * STREAMLINED: Refresh all data
     */
    fun refresh(ppid: String) {
        AppUtils.logInfo(TAG, "Refreshing all data for PPID: $ppid")
        loadProfile(ppid)
        // Transaction logs will be auto-loaded when profile loads successfully
    }

    /**
     * Clear action result state
     */
    fun clearActionResult() {
        _blockUnblockResult.value = Resource.Empty
    }

    /**
     * Get current profile data
     */
    fun getCurrentProfile(): LoketProfile? {
        return (_profileState.value as? Resource.Success)?.data
    }

    /**
     * Check if loket is blocked
     */
    fun isLoketBlocked(): Boolean {
        return getCurrentProfile()?.isBlocked() ?: false
    }

    /**
     * Get transaction statistics
     */
    fun getTransactionStats(): TransactionStats? {
        val transactions = (_transactionLogs.value as? Resource.Success)?.data ?: return null

        val incoming = transactions.filter { it.isIncomingTransaction() }
        val outgoing = transactions.filter { it.isOutgoingTransaction() }

        return TransactionStats(
            totalTransactions = transactions.size,
            incomingCount = incoming.size,
            outgoingCount = outgoing.size,
            totalIncoming = incoming.sumOf { it.tldAmount },
            totalOutgoing = outgoing.sumOf { kotlin.math.abs(it.tldAmount) },
            latestBalance = transactions.firstOrNull()?.tldBalance ?: 0L
        )
    }

    /**
     * HELPER: Create placeholder transaction logs for testing
     */
    private fun createPlaceholderTransactionLogs(ppid: String) {
        try {
            val placeholderTransactions = AppUtils.createPlaceholderTransactionLogs(ppid, 7)
            _transactionLogs.value = Resource.Success(placeholderTransactions)
            AppUtils.logInfo(TAG, "Created placeholder transaction logs: ${placeholderTransactions.size} items")
        } catch (e: Exception) {
            AppUtils.logError(TAG, "Failed to create placeholder data", e)
        }
    }

    /**
     * STREAMLINED: Get debug info
     */
    fun getDebugInfo(): String {
        val currentProfile = getCurrentProfile()
        val stats = getTransactionStats()

        return """
        DetailLoketViewModel Debug Info:
        - Current PPID: ${currentProfile?.ppid ?: "None"}
        - Loket Name: ${currentProfile?.namaLoket ?: "None"}
        - Status: ${currentProfile?.status ?: "None"}
        - Is Blocked: ${isLoketBlocked()}
        - Profile State: ${_profileState.value.javaClass.simpleName}
        - Transaction State: ${_transactionLogs.value.javaClass.simpleName}
        - Action State: ${_blockUnblockResult.value.javaClass.simpleName}
        - Total Transactions: ${stats?.totalTransactions ?: 0}
        - Latest Balance: ${stats?.latestBalance?.let { AppUtils.formatCurrency(it) } ?: "N/A"}
        """.trimIndent()
    }

    data class TransactionStats(
        val totalTransactions: Int,
        val incomingCount: Int,
        val outgoingCount: Int,
        val totalIncoming: Long,
        val totalOutgoing: Long,
        val latestBalance: Long
    )

    override fun onCleared() {
        super.onCleared()
        AppUtils.logInfo(TAG, "DetailLoketViewModel cleared")
    }
}
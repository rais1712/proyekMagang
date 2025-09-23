// File: app/src/main/java/com/proyek/maganggsp/presentation/transaction/TransactionLogViewModel.kt
package com.proyek.maganggsp.presentation.transaction

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.usecase.GetTransactionLogsUseCase
import com.proyek.maganggsp.util.NavigationConstants
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * NEW: TransactionLog detail screen ViewModel
 * Separate screen untuk menampilkan transaction logs dari /trx/ppid/{ppid}
 * Navigation: Receipt click → TransactionLogActivity
 */
@HiltViewModel
class TransactionLogViewModel @Inject constructor(
    private val getTransactionLogsUseCase: GetTransactionLogsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "TransactionLogViewModel"
    }

    private val _transactionLogsState = MutableStateFlow<Resource<List<TransactionLog>>>(Resource.Loading())
    val transactionLogsState: StateFlow<Resource<List<TransactionLog>>> = _transactionLogsState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val currentPpid: String

    init {
        // Extract PPID dari navigation arguments
        currentPpid = savedStateHandle.get<String>(NavigationConstants.ARG_PPID)
            ?: savedStateHandle.get<String>("ppid")
                    ?: ""

        AppUtils.logInfo(TAG, "TransactionLogViewModel initialized with PPID: $currentPpid")

        if (currentPpid.isNotEmpty()) {
            refreshData()
        } else {
            _transactionLogsState.value = Resource.Error(
                com.proyek.maganggsp.util.exceptions.AppException.ValidationException("PPID tidak valid")
            )
        }
    }

    fun refreshData() {
        if (currentPpid.isEmpty()) return

        AppUtils.logInfo(TAG, "Loading transaction logs for PPID: $currentPpid")

        getTransactionLogsUseCase(currentPpid).onEach { result ->
            _transactionLogsState.value = result

            when (result) {
                is Resource.Success -> {
                    AppUtils.logInfo(TAG, "Transaction logs loaded: ${result.data.size} transactions")

                    // Create placeholder data jika empty untuk testing
                    if (result.data.isEmpty()) {
                        createPlaceholderData()
                    }
                }
                is Resource.Error -> {
                    AppUtils.logError(TAG, "Transaction logs error", result.exception)
                    // Create placeholder data untuk testing
                    createPlaceholderData()
                }
                is Resource.Loading -> {
                    AppUtils.logDebug(TAG, "Loading transaction logs...")
                }
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    private fun createPlaceholderData() {
        val placeholderTransactions = AppUtils.createPlaceholderTransactionLogs(currentPpid, 7)
        _transactionLogsState.value = Resource.Success(placeholderTransactions)
        AppUtils.logInfo(TAG, "Created placeholder transaction logs: ${placeholderTransactions.size} items")
    }

    fun getCurrentPpid(): String = currentPpid

    fun getTransactionStats(): TransactionStats? {
        val transactions = (_transactionLogsState.value as? Resource.Success)?.data ?: return null

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

    private fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    override fun onCleared() {
        super.onCleared()
        AppUtils.logInfo(TAG, "TransactionLogViewModel cleared")
    }

    data class TransactionStats(
        val totalTransactions: Int,
        val incomingCount: Int,
        val outgoingCount: Int,
        val totalIncoming: Long,
        val totalOutgoing: Long,
        val latestBalance: Long
    )

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
    }
}


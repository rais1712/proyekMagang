// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/DetailLoketViewModel.kt - NEW
package com.proyek.maganggsp.presentation.detail_loket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.usecase.loket.GetLoketProfileUseCase
import com.proyek.maganggsp.domain.usecase.loket.GetLoketTransactionsUseCase
import com.proyek.maganggsp.util.NavigationConstants
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class DetailLoketViewModel @Inject constructor(
    private val getLoketProfileUseCase: GetLoketProfileUseCase,
    private val getLoketTransactionsUseCase: GetLoketTransactionsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "DetailLoketViewModel"
        private const val PLACEHOLDER_PPID = "PIDLKTD0025blok" // Placeholder untuk testing
    }

    // Loket profile state
    private val _loketProfileState = MutableStateFlow<Resource<Loket>>(Resource.Loading())
    val loketProfileState: StateFlow<Resource<Loket>> = _loketProfileState.asStateFlow()

    // Transaction logs state
    private val _transactionLogsState =
        MutableStateFlow<Resource<List<TransactionLog>>>(Resource.Loading())
    val transactionLogsState: StateFlow<Resource<List<TransactionLog>>> =
        _transactionLogsState.asStateFlow()

    // Profile update action state
    private val _actionState = MutableStateFlow<Resource<Unit>>(Resource.Empty)
    val actionState: StateFlow<Resource<Unit>> = _actionState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentPpid: String? = null

    init {
        Log.d(TAG, "DetailLoketViewModel initialized untuk loket management")

        // Get ppid dari SavedStateHandle
        currentPpid = extractPpidFromArguments(savedStateHandle)
        Log.d(TAG, "Using ppid: $currentPpid untuk loket detail")

        if (currentPpid != null) {
            refreshData()
        } else {
            _loketProfileState.value = Resource.Error(
                AppException.ValidationException("Identifier tidak valid")
            )
            _transactionLogsState.value = Resource.Error(
                AppException.ValidationException("Identifier tidak valid")
            )
        }
    }

    private fun extractPpidFromArguments(savedStateHandle: SavedStateHandle): String? {
        // Try beberapa kemungkinan argument key
        val possibleKeys = listOf(
            NavigationConstants.ARG_PPID,
            "ppid",
            "noLoket", // Legacy compatibility
            NavigationConstants.ARG_NO_LOKET
        )

        for (key in possibleKeys) {
            val value = savedStateHandle.get<String>(key)
            if (!value.isNullOrBlank()) {
                Log.d(TAG, "Found argument: $key = $value")
                return validateAndProcessPpid(value)
            }
        }

        // PLACEHOLDER: Jika tidak ada argument, gunakan placeholder untuk testing
        Log.w(TAG, "Tidak ada ppid argument, menggunakan placeholder untuk testing")
        return PLACEHOLDER_PPID
    }

    private fun validateAndProcessPpid(identifier: String): String {
        return when {
            // Jika sudah format ppid yang benar
            identifier.startsWith("PID") && identifier.length > 10 -> {
                Log.d(TAG, "Valid ppid format: $identifier")
                identifier
            }

            // Jika identifier panjang (kemungkinan ppid atau refNumber)
            identifier.length > 15 -> {
                Log.d(TAG, "Long identifier, using as ppid: $identifier")
                identifier
            }

            // Fallback ke placeholder
            else -> {
                Log.w(TAG, "Invalid identifier format: $identifier, using placeholder")
                PLACEHOLDER_PPID
            }
        }
    }

    // Hapus fungsi updateLoketProfile karena tidak digunakan
    fun refreshData() {
        currentPpid?.let { ppid ->
            Log.d(TAG, "Refresh data untuk ppid: $ppid")
            loadLoketProfile(ppid)
            loadTransactionLogs(ppid)
        }
    }

    private fun loadLoketProfile(ppid: String) {
        getLoketProfileUseCase(ppid).onEach { result ->
            _loketProfileState.value = result

            when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "Loket profile dimuat: ${result.data?.namaLoket}")
                }

                is Resource.Error -> {
                    Log.e(TAG, "Loket profile error: ${result.message}")
                }

                is Resource.Loading -> {
                    Log.d(TAG, "Memuat loket profile...")
                }

                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    private fun loadTransactionLogs(ppid: String) {
        getLoketTransactionsUseCase(ppid).onEach { result ->
            _transactionLogsState.value = result

            when (result) {
                is Resource.Success -> {
                    val data = result.data ?: emptyList()
                    Log.d(TAG, "Transaction logs dimuat: ${data.size} items")
                }

                is Resource.Error -> {
                    Log.e(TAG, "Transaction logs error: ${result.message}")
                }

                is Resource.Loading -> {
                    Log.d(TAG, "Memuat transaction logs...")
                }

                else -> Unit
            }
        }.launchIn(viewModelScope)
    }


    private fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    fun getCurrentPpid(): String? = currentPpid

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "DetailLoketViewModel cleared")
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
        data class NavigateToUpdateProfile(val ppid: String) : UiEvent()
    }
}
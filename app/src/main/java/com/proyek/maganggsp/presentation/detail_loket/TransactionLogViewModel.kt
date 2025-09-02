// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/TransactionLogViewModel.kt
package com.proyek.maganggsp.presentation.detail_loket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.domain.usecase.profile.GetTransactionLogsUseCase
import com.proyek.maganggsp.domain.usecase.profile.UpdateProfileUseCase
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class TransactionLogViewModel @Inject constructor(
    private val getTransactionLogsUseCase: GetTransactionLogsUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "TransactionLogViewModel"
        private const val DEFAULT_PPID = "PIDLKTD0025blok" // Default PPID for testing
    }

    // Transaction logs state (replaces mutations)
    private val _transactionLogsState = MutableStateFlow<Resource<List<TransactionLog>>>(Resource.Loading())
    val transactionLogsState: StateFlow<Resource<List<TransactionLog>>> = _transactionLogsState.asStateFlow()

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
        Log.d(TAG, "🔄 TransactionLogViewModel initialized for new data structure")

        // Get the identifier from SavedStateHandle (could be refNumber from receipt)
        val identifier = savedStateHandle.get<String>("noLoket") ?: DEFAULT_PPID
        currentPpid = mapIdentifierToPpid(identifier)

        Log.d(TAG, "📋 Using PPID: $currentPpid for transaction logs")

        if (currentPpid != null) {
            refreshData()
        } else {
            _transactionLogsState.value = Resource.Error(
                AppException.ValidationException("Invalid identifier provided")
            )
        }
    }

    private fun mapIdentifierToPpid(identifier: String): String {
        // Map the received identifier to a PPID
        // For now, use default PPID or the identifier itself if it looks like a PPID
        return if (identifier.startsWith("PID") || identifier.length > 10) {
            identifier
        } else {
            DEFAULT_PPID // Use default PPID for testing
        }
    }

    fun refreshData() {
        currentPpid?.let { ppid ->
            Log.d(TAG, "🔄 Refreshing transaction logs for PPID: $ppid")
            loadTransactionLogs(ppid)
        }
    }

    private fun loadTransactionLogs(ppid: String) {
        getTransactionLogsUseCase(ppid).onEach { result ->
            _transactionLogsState.value = result

            when (result) {
                is Resource.Success -> Log.d(TAG, "✅ Transaction logs loaded: ${result.data?.size ?: 0} items")
                is Resource.Error -> Log.e(TAG, "❌ Transaction logs error: ${result.message}")
                is Resource.Loading -> Log.d(TAG, "⏳ Loading transaction logs...")
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    fun updateProfile(newPpid: String) {
        currentPpid?.let { ppid ->
            Log.d(TAG, "🔄 Updating profile from $ppid to $newPpid")

            updateProfileUseCase(ppid, newPpid).onEach { result ->
                _actionState.value = result

                when (result) {
                    is Resource.Success -> {
                        emitUiEvent(UiEvent.ShowToast("Profile updated successfully"))
                        currentPpid = newPpid
                        refreshData() // Refresh with new PPID
                        Log.d(TAG, "✅ Profile updated successfully")
                    }
                    is Resource.Error -> {
                        emitUiEvent(UiEvent.ShowToast("Failed to update profile: ${result.message}"))
                        Log.e(TAG, "❌ Profile update error: ${result.message}")
                    }
                    is Resource.Loading -> {
                        Log.d(TAG, "⏳ Updating profile...")
                    }
                    else -> Unit
                }
            }.launchIn(viewModelScope)
        }
    }

    fun onActionConsumed() {
        _actionState.value = Resource.Empty
    }

    private fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 TransactionLogViewModel cleared")
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
    }
}
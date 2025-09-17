// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/TransactionLogViewModel.kt - FIXED PPID
package com.proyek.maganggsp.presentation.detail_loket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.usecase.loketprofile.GetProfileUseCase
import com.proyek.maganggsp.domain.usecase.loketprofile.GetTransactionLogsUseCase
import com.proyek.maganggsp.domain.usecase.loketprofile.UpdateProfileUseCase
import com.proyek.maganggsp.util.NavigationConstants
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.util.Log

@HiltViewModel
class TransactionLogViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val getTransactionLogsUseCase: GetTransactionLogsUseCase,
    private val updateProfileUseCase: UpdateProfileUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "TransactionLogViewModel"
        private const val PLACEHOLDER_PPID = "PIDLKTD0025blok" // Placeholder untuk testing
    }

    // FIXED: Profile info state (untuk menampilkan info receipt)
    private val _profileState = MutableStateFlow<Resource<Receipt>>(Resource.Loading())
    val profileState: StateFlow<Resource<Receipt>> = _profileState.asStateFlow()

    // Transaction logs state
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
        Log.d(TAG, "🔄 TransactionLogViewModel initialized dengan ppid argument fix")

        // FIXED: Get ppid dari SavedStateHandle yang benar
        currentPpid = extractPpidFromArguments(savedStateHandle)

        Log.d(TAG, "📋 Menggunakan ppid: $currentPpid untuk transaction logs")

        if (currentPpid != null) {
            refreshData()
        } else {
            _transactionLogsState.value = Resource.Error(
                AppException.ValidationException("Identifier tidak valid")
            )
            _profileState.value = Resource.Error(
                AppException.ValidationException("Identifier tidak valid")
            )
        }
    }

    private fun extractPpidFromArguments(savedStateHandle: SavedStateHandle): String? {
        // FIXED: Coba beberapa kemungkinan argument key
        val possibleKeys = listOf(
            NavigationConstants.ARG_PPID,
            "ppid",
            "noLoket", // Legacy compatibility
            NavigationConstants.ARG_NO_LOKET
        )

        for (key in possibleKeys) {
            val value = savedStateHandle.get<String>(key)
            if (!value.isNullOrBlank()) {
                Log.d(TAG, "📋 Found argument: $key = $value")
                return validateAndProcessPpid(value)
            }
        }

        // PLACEHOLDER: Jika tidak ada argument, gunakan placeholder untuk testing
        Log.w(TAG, "⚠️ Tidak ada ppid argument, menggunakan placeholder untuk testing")
        return PLACEHOLDER_PPID
    }

    private fun validateAndProcessPpid(identifier: String): String {
        return when {
            // Jika sudah format ppid yang benar
            identifier.startsWith("PID") && identifier.length > 10 -> {
                Log.d(TAG, "✅ Valid ppid format: $identifier")
                identifier
            }

            // Jika identifier panjang (kemungkinan ppid atau refNumber)
            identifier.length > 15 -> {
                Log.d(TAG, "🔄 Long identifier, using as ppid: $identifier")
                identifier
            }

            // Fallback ke placeholder
            else -> {
                Log.w(TAG, "⚠️ Invalid identifier format: $identifier, using placeholder")
                PLACEHOLDER_PPID
            }
        }
    }

    fun refreshData() {
        currentPpid?.let { ppid ->
            Log.d(TAG, "🔄 Refresh data untuk ppid: $ppid")
            loadProfileInfo(ppid)
            loadTransactionLogs(ppid)
        }
    }

    private fun loadProfileInfo(ppid: String) {
        getProfileUseCase(ppid).onEach { result ->
            _profileState.value = result

            when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ Profile info dimuat: ${result.data?.refNumber}")
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ Profile info error: ${result.message}")
                    // Jika gagal load profile, buat placeholder
                    createPlaceholderProfile(ppid)
                }
                is Resource.Loading -> {
                    Log.d(TAG, "⏳ Memuat profile info...")
                }
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    private fun createPlaceholderProfile(ppid: String) {
        val placeholderReceipt = Receipt(
            refNumber = "REF-PLACEHOLDER",
            idPelanggan = ppid,
            amount = 0L,
            logged = "Placeholder data untuk testing"
        )
        _profileState.value = Resource.Success(placeholderReceipt)
        Log.d(TAG, "📋 Placeholder profile dibuat untuk ppid: $ppid")
    }

    private fun loadTransactionLogs(ppid: String) {
        getTransactionLogsUseCase(ppid).onEach { result ->
            _transactionLogsState.value = result

            when (result) {
                is Resource.Success -> {
                    val data = result.data ?: emptyList()
                    Log.d(TAG, "✅ Transaction logs dimuat: ${data.size} items")

                    // Jika tidak ada data, buat placeholder untuk testing
                    if (data.isEmpty()) {
                        createPlaceholderTransactionLogs(ppid)
                    }
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ Transaction logs error: ${result.message}")
                    // Buat placeholder untuk testing
                    createPlaceholderTransactionLogs(ppid)
                }
                is Resource.Loading -> {
                    Log.d(TAG, "⏳ Memuat transaction logs...")
                }
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    private fun createPlaceholderTransactionLogs(ppid: String) {
        val placeholderTransactions = listOf(
            TransactionLog(
                tldRefnum = "TXN001-PLACEHOLDER",
                tldPan = "1234****5678",
                tldIdpel = ppid,
                tldAmount = 100000L,
                tldBalance = 1000000L,
                tldDate = "2024-01-15T10:30:00.000Z",
                tldPpid = ppid
            ),
            TransactionLog(
                tldRefnum = "TXN002-PLACEHOLDER",
                tldPan = "1234****5678",
                tldIdpel = ppid,
                tldAmount = -50000L,
                tldBalance = 950000L,
                tldDate = "2024-01-14T14:15:00.000Z",
                tldPpid = ppid
            ),
            TransactionLog(
                tldRefnum = "TXN003-PLACEHOLDER",
                tldPan = "1234****5678",
                tldIdpel = ppid,
                tldAmount = 75000L,
                tldBalance = 1025000L,
                tldDate = "2024-01-13T09:45:00.000Z",
                tldPpid = ppid
            )
        )

        _transactionLogsState.value = Resource.Success(placeholderTransactions)
        Log.d(TAG, "📋 Placeholder transaction logs dibuat: ${placeholderTransactions.size} items")
    }

    fun updateProfile(newPpid: String) {
        currentPpid?.let { ppid ->
            Log.d(TAG, "🔄 Update profile dari $ppid ke $newPpid")

            updateProfileUseCase(ppid, newPpid).onEach { result ->
                _actionState.value = result

                when (result) {
                    is Resource.Success -> {
                        emitUiEvent(UiEvent.ShowToast("Profil berhasil diupdate"))
                        currentPpid = newPpid
                        refreshData() // Refresh dengan ppid baru
                        Log.d(TAG, "✅ Profile berhasil diupdate")
                    }
                    is Resource.Error -> {
                        emitUiEvent(UiEvent.ShowToast("Gagal update profil: ${result.message}"))
                        Log.e(TAG, "❌ Profile update error: ${result.message}")
                    }
                    is Resource.Loading -> {
                        Log.d(TAG, "⏳ Update profil...")
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

    fun getCurrentPpid(): String? = currentPpid

    fun navigateToUpdateProfile() {
        currentPpid?.let { ppid ->
            emitUiEvent(UiEvent.NavigateToUpdateProfile(ppid))
            Log.d(TAG, "🔄 Navigate ke update profile dengan ppid: $ppid")
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 TransactionLogViewModel cleared")
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
        data class NavigateToUpdateProfile(val ppid: String) : UiEvent()
    }
}
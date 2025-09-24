// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/DetailLoketViewModel.kt
package com.proyek.maganggsp.presentation.detail_loket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.usecase.*
import com.proyek.maganggsp.util.NavigationConstants
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * REFACTORED: DetailLoketViewModel focused on profile info + receipt display
 * Shows: Profile card + Receipt list (Receipt click navigates to TransactionLog)
 * Block/Unblock: Uses unified use cases
 */
@HiltViewModel
class DetailLoketViewModel @Inject constructor(
    private val getProfileUseCase: GetProfileUseCase,
    private val blockUnblockUseCase: BlockUnblockUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "DetailLoketViewModel"
    }

    private val _profileState = MutableStateFlow<Resource<Receipt>>(Resource.Loading())
    val profileState: StateFlow<Resource<Receipt>> = _profileState.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<Unit>>(Resource.Empty)
    val actionState: StateFlow<Resource<Unit>> = _actionState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private val currentPpid: String

    init {
        // Extract PPID dari navigation arguments
        currentPpid = savedStateHandle.get<String>(NavigationConstants.ARG_PPID)
            ?: savedStateHandle.get<String>("ppid")
                    ?: ""

        AppUtils.logInfo(TAG, "DetailLoketViewModel initialized with PPID: $currentPpid")

        if (currentPpid.isNotEmpty()) {
            refreshData()
        } else {
            _profileState.value = Resource.Error(
                com.proyek.maganggsp.util.exceptions.AppException.ValidationException("PPID tidak valid")
            )
        }
    }

    fun refreshData() {
        if (currentPpid.isEmpty()) return

        AppUtils.logInfo(TAG, "Loading profile for PPID: $currentPpid")

        getProfileUseCase(currentPpid).onEach { result ->
            _profileState.value = result

            when (result) {
                is Resource.Success -> {
                    AppUtils.logInfo(TAG, "Profile loaded successfully: ${result.data.refNumber}")
                }
                is Resource.Error -> {
                    AppUtils.logError(TAG, "Profile load error", result.exception)
                    // Create placeholder untuk testing
                    createPlaceholderProfile()
                }
                is Resource.Loading -> {
                    AppUtils.logDebug(TAG, "Loading profile...")
                }
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    private fun createPlaceholderProfile() {
        val placeholderReceipt = AppUtils.createPlaceholderReceipt(currentPpid)
        _profileState.value = Resource.Success(placeholderReceipt)
        AppUtils.logInfo(TAG, "Created placeholder profile for PPID: $currentPpid")
    }

    fun blockProfile() {
        AppUtils.logInfo(TAG, "Blocking profile: $currentPpid")

        blockUnblockUseCase.blockProfile(currentPpid).onEach { result ->
            _actionState.value = result

            when (result) {
                is Resource.Success -> {
                    emitUiEvent(UiEvent.ShowToast("Profil berhasil diblokir"))
                    refreshData() // Refresh untuk update status
                }
                is Resource.Error -> {
                    emitUiEvent(UiEvent.ShowToast("Gagal memblokir profil: ${result.exception.message}"))
                }
                is Resource.Loading -> {
                    AppUtils.logDebug(TAG, "Block operation in progress...")
                }
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    fun unblockProfile() {
        AppUtils.logInfo(TAG, "Unblocking profile: $currentPpid")

        blockUnblockUseCase.unblockProfile(currentPpid).onEach { result ->
            _actionState.value = result

            when (result) {
                is Resource.Success -> {
                    emitUiEvent(UiEvent.ShowToast("Profil berhasil dibuka blokirnya"))
                    refreshData() // Refresh untuk update status
                }
                is Resource.Error -> {
                    emitUiEvent(UiEvent.ShowToast("Gagal membuka blokir profil: ${result.exception.message}"))
                }
                is Resource.Loading -> {
                    AppUtils.logDebug(TAG, "Unblock operation in progress...")
                }
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    fun onActionConsumed() {
        _actionState.value = Resource.Empty
    }

    fun isProfileBlocked(): Boolean {
        val profile = (_profileState.value as? Resource.Success)?.data
        return blockUnblockUseCase.isBlocked(profile?.ppid ?: "")
    }

    fun navigateToTransactionLog(receipt: Receipt) {
        emitUiEvent(UiEvent.NavigateToTransactionLog(receipt.ppid))
        AppUtils.logInfo(TAG, "Navigate to transaction log with PPID: ${receipt.ppid}")
    }

    fun getCurrentPpid(): String = currentPpid

    private fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    override fun onCleared() {
        super.onCleared()
        AppUtils.logInfo(TAG, "DetailLoketViewModel cleared")
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data class NavigateToTransactionLog(val ppid: String) : UiEvent()
        object NavigateBack : UiEvent()
    }
}
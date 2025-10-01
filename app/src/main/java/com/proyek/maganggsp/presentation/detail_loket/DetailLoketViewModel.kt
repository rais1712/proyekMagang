package com.proyek.maganggsp.presentation.detail_loket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.usecase.profile.GetProfileUseCase
import com.proyek.maganggsp.domain.usecase.profile.BlockUnblockUseCase
import com.proyek.maganggsp.util.NavigationConstants
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.LoggingUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel untuk DetailLoketActivity
 * Mengelola data profil dan aksi block/unblock
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
        currentPpid = savedStateHandle.get<String>(NavigationConstants.ARG_PPID)
            ?: savedStateHandle.get<String>("ppid")
            ?: ""
            
        // Menerapkan extractPpidSafely untuk konsistensi
        currentPpid = currentPpid.extractPpidSafely()

        LoggingUtils.logInfo(TAG, "ViewModel initialized with PPID: $currentPpid")

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

        LoggingUtils.logInfo(TAG, "Loading profile for PPID: $currentPpid")

        getProfileUseCase(currentPpid).onEach { result ->
            _profileState.value = result

            when (result) {
                is Resource.Success -> {
                    LoggingUtils.logInfo(TAG, "Profile loaded successfully: ${result.data.refNumber}")
                }
                is Resource.Error -> {
                    LoggingUtils.logError(TAG, "Profile load error", result.exception)
                    emitUiEvent(UiEvent.ShowToast("Gagal memuat data: ${result.exception.message}"))
                }
                is Resource.Loading -> {
                    LoggingUtils.logDebug(TAG, "Loading profile...")
                }
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    fun blockProfile() {
        LoggingUtils.logInfo(TAG, "Blocking profile: $currentPpid")

        blockUnblockUseCase.blockProfile(currentPpid).onEach { result ->
            _actionState.value = result

            when (result) {
                is Resource.Success -> {
                    emitUiEvent(UiEvent.ShowToast("Profil berhasil diblokir"))
                    refreshData()
                }
                is Resource.Error -> {
                    emitUiEvent(UiEvent.ShowToast("Gagal memblokir profil: ${result.exception.message}"))
                }
                is Resource.Loading -> {
                    LoggingUtils.logDebug(TAG, "Block operation in progress...")
                }
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    fun unblockProfile() {
        LoggingUtils.logInfo(TAG, "Unblocking profile: $currentPpid")

        blockUnblockUseCase.unblockProfile(currentPpid).onEach { result ->
            _actionState.value = result

            when (result) {
                is Resource.Success -> {
                    emitUiEvent(UiEvent.ShowToast("Profil berhasil dibuka blokirnya"))
                    refreshData()
                }
                is Resource.Error -> {
                    emitUiEvent(UiEvent.ShowToast("Gagal membuka blokir profil: ${result.exception.message}"))
                }
                is Resource.Loading -> {
                    LoggingUtils.logDebug(TAG, "Unblock operation in progress...")
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
        LoggingUtils.logInfo(TAG, "Navigate to transaction log with PPID: ${receipt.ppid}")
    }

    fun getCurrentPpid(): String = currentPpid

    private fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    override fun onCleared() {
        super.onCleared()
        LoggingUtils.logInfo(TAG, "DetailLoketViewModel cleared")
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data class NavigateToTransactionLog(val ppid: String) : UiEvent()
        object NavigateBack : UiEvent()
    }
}
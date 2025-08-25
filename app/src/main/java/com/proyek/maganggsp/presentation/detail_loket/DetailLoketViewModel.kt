// FIXED: Standardized Resource usage across all ViewModels
// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/DetailLoketViewModel.kt

package com.proyek.maganggsp.presentation.detail_loket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.Mutasi
import com.proyek.maganggsp.domain.usecase.loket.*
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.FeatureFlags
import android.util.Log
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class DetailLoketViewModel @Inject constructor(
    private val getLoketDetailUseCase: GetLoketDetailUseCase,
    private val getMutationUseCase: GetMutationUseCase,
    private val blockLoketUseCase: BlockLoketUseCase,
    private val unblockLoketUseCase: UnblockLoketUseCase,
    private val clearAllFlagsUseCase: ClearAllFlagsUseCase,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val TAG = "DetailLoketViewModel"
    }

    // FIXED: Consistent usage of Resource.Empty as object
    private val _loketDetailsState = MutableStateFlow<Resource<Loket>>(Resource.Loading())
    val loketDetailsState: StateFlow<Resource<Loket>> = _loketDetailsState.asStateFlow()

    private val _mutationsState = MutableStateFlow<Resource<List<Mutasi>>>(Resource.Loading())
    val mutationsState: StateFlow<Resource<List<Mutasi>>> = _mutationsState.asStateFlow()

    private val _actionState = MutableStateFlow<Resource<Unit>>(Resource.Empty)
    val actionState: StateFlow<Resource<Unit>> = _actionState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentNoLoket: String? = null

    init {
        // 🚩 FEATURE FLAGS: Debug logging for ViewModel initialization
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 DetailLoketViewModel initialized")
            Log.d(TAG, "Detail view enabled: ${FeatureFlags.ENABLE_LOKET_DETAIL_VIEW}")
            Log.d(TAG, "Actions enabled: ${FeatureFlags.ENABLE_LOKET_ACTIONS}")
            Log.d(TAG, "Mutations enabled: ${FeatureFlags.ENABLE_MUTATION_HISTORY}")
        }

        currentNoLoket = savedStateHandle.get<String>("noLoket")
        if (currentNoLoket != null) {
            refreshData()
        } else {
            // Handle missing parameter gracefully
            _loketDetailsState.value = Resource.Error(
                com.proyek.maganggsp.util.exceptions.AppException.ValidationException(
                    "Nomor loket tidak ditemukan"
                )
            )

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.e(TAG, "🚩 No loket parameter found in SavedStateHandle")
            }
        }
    }

    fun refreshData() {
        currentNoLoket?.let { noLoket ->
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Refreshing data for loket: $noLoket")
            }

            // Always load loket details if view is enabled
            if (FeatureFlags.ENABLE_LOKET_DETAIL_VIEW) {
                loadLoketDetails(noLoket)
            }

            // Load mutations only if enabled
            if (FeatureFlags.ENABLE_MUTATION_HISTORY) {
                loadMutations(noLoket)
            } else {
                // Set empty state for mutations when disabled
                _mutationsState.value = Resource.Success(emptyList())

                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.w(TAG, "🚩 Mutations loading disabled by feature flag")
                }
            }
        }
    }

    private fun loadLoketDetails(noLoket: String) {
        // 🚩 FEATURE FLAGS: Only load if real data loading enabled
        if (!FeatureFlags.ENABLE_REAL_DATA_LOADING) {
            _loketDetailsState.value = Resource.Error(
                com.proyek.maganggsp.util.exceptions.AppException.ValidationException(
                    "Fitur detail loket sedang dikembangkan"
                )
            )
            return
        }

        getLoketDetailUseCase(noLoket)
            .onEach { result ->
                _loketDetailsState.value = result

                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    when (result) {
                        is Resource.Success -> Log.d(TAG, "🚩 Loket details loaded: ${result.data.noLoket}")
                        is Resource.Error -> Log.e(TAG, "🚩 Loket details error: ${result.message}")
                        else -> Log.d(TAG, "🚩 Loket details state: ${result::class.simpleName}")
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    private fun loadMutations(noLoket: String) {
        // 🚩 FEATURE FLAGS: Only load if enabled and real data loading enabled
        if (!FeatureFlags.ENABLE_MUTATION_HISTORY || !FeatureFlags.ENABLE_REAL_DATA_LOADING) {
            _mutationsState.value = Resource.Success(emptyList())
            return
        }

        getMutationUseCase(noLoket)
            .onEach { result ->
                _mutationsState.value = result

                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    when (result) {
                        is Resource.Success -> Log.d(TAG, "🚩 Mutations loaded: ${result.data?.size ?: 0} items")
                        is Resource.Error -> Log.e(TAG, "🚩 Mutations error: ${result.message}")
                        else -> Log.d(TAG, "🚩 Mutations state: ${result::class.simpleName}")
                    }
                }
            }
            .launchIn(viewModelScope)
    }

    fun blockLoket() {
        // 🚩 FEATURE FLAGS: Only allow if actions are enabled
        if (!FeatureFlags.ENABLE_LOKET_ACTIONS) {
            emitUiEvent(UiEvent.ShowToast("Fitur blokir sedang dikembangkan"))

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Block action blocked by feature flag")
            }
            return
        }

        currentNoLoket?.let { noLoket ->
            blockLoketUseCase(noLoket)
                .onEach { result ->
                    _actionState.value = result
                    if (result is Resource.Success) {
                        emitUiEvent(UiEvent.ShowToast("Loket berhasil diblokir"))
                        refreshData() // Refresh to get updated status
                    }

                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.d(TAG, "🚩 Block action result: ${result::class.simpleName}")
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun unblockLoket() {
        // 🚩 FEATURE FLAGS: Only allow if actions are enabled
        if (!FeatureFlags.ENABLE_LOKET_ACTIONS) {
            emitUiEvent(UiEvent.ShowToast("Fitur buka blokir sedang dikembangkan"))

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Unblock action blocked by feature flag")
            }
            return
        }

        currentNoLoket?.let { noLoket ->
            unblockLoketUseCase(noLoket)
                .onEach { result ->
                    _actionState.value = result
                    if (result is Resource.Success) {
                        emitUiEvent(UiEvent.ShowToast("Blokir loket berhasil dibuka"))
                        refreshData() // Refresh to get updated status
                    }

                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.d(TAG, "🚩 Unblock action result: ${result::class.simpleName}")
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun clearAllFlags() {
        // 🚩 FEATURE FLAGS: Only allow if flag management is enabled
        if (!FeatureFlags.ENABLE_FLAG_MANAGEMENT) {
            emitUiEvent(UiEvent.ShowToast("Fitur hapus penanda sedang dikembangkan"))

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Clear flags action blocked by feature flag")
            }
            return
        }

        currentNoLoket?.let { noLoket ->
            clearAllFlagsUseCase(noLoket)
                .onEach { result ->
                    _actionState.value = result
                    if (result is Resource.Success) {
                        emitUiEvent(UiEvent.ShowToast("Semua tanda berhasil dihapus"))
                        refreshData() // Refresh to get updated status
                    }

                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.d(TAG, "🚩 Clear flags action result: ${result::class.simpleName}")
                    }
                }
                .launchIn(viewModelScope)
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

    // ENHANCED: Better lifecycle management
    override fun onCleared() {
        super.onCleared()

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 DetailLoketViewModel cleared")
        }
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }
}
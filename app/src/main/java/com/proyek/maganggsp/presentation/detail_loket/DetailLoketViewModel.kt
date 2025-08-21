// FIXED: Standardized Resource usage across all ViewModels
// File: app/src/main/java/com/proyek/maganggsp/presentation/detailloket/DetailLoketViewModel.kt

package com.proyek.maganggsp.presentation.detailloket

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.Mutasi
import com.proyek.maganggsp.domain.usecase.loket.*
import com.proyek.maganggsp.util.Resource
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
        }
    }

    fun refreshData() {
        currentNoLoket?.let { noLoket ->
            loadLoketDetails(noLoket)
            loadMutations(noLoket)
        }
    }

    private fun loadLoketDetails(noLoket: String) {
        getLoketDetailUseCase(noLoket)
            .onEach { result ->
                _loketDetailsState.value = result
            }
            .launchIn(viewModelScope)
    }

    private fun loadMutations(noLoket: String) {
        getMutationUseCase(noLoket)
            .onEach { result ->
                _mutationsState.value = result
            }
            .launchIn(viewModelScope)
    }

    fun blockLoket() {
        currentNoLoket?.let { noLoket ->
            blockLoketUseCase(noLoket)
                .onEach { result ->
                    _actionState.value = result
                    if (result is Resource.Success) {
                        emitUiEvent(UiEvent.ShowToast("Loket berhasil diblokir"))
                        refreshData() // Refresh to get updated status
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun unblockLoket() {
        currentNoLoket?.let { noLoket ->
            unblockLoketUseCase(noLoket)
                .onEach { result ->
                    _actionState.value = result
                    if (result is Resource.Success) {
                        emitUiEvent(UiEvent.ShowToast("Blokir loket berhasil dibuka"))
                        refreshData() // Refresh to get updated status
                    }
                }
                .launchIn(viewModelScope)
        }
    }

    fun clearAllFlags() {
        currentNoLoket?.let { noLoket ->
            clearAllFlagsUseCase(noLoket)
                .onEach { result ->
                    _actionState.value = result
                    if (result is Resource.Success) {
                        emitUiEvent(UiEvent.ShowToast("Semua tanda berhasil dihapus"))
                        refreshData() // Refresh to get updated status
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
        // Clean up any ongoing operations if needed
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        object NavigateBack : UiEvent()
        data class ShowError(val message: String) : UiEvent()
    }
}
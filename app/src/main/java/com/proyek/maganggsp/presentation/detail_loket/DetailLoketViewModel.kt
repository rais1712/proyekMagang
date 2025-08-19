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

    // --- KESALAHAN UTAMA DIPERBAIKI DI SINI ---
    // Hapus tanda kurung () dari Loading dan Empty
    private val _loketDetailsState = MutableStateFlow<Resource<Loket>>(Resource.Loading)
    val loketDetailsState: StateFlow<Resource<Loket>> = _loketDetailsState

    private val _mutationsState = MutableStateFlow<Resource<List<Mutasi>>>(Resource.Loading)
    val mutationsState: StateFlow<Resource<List<Mutasi>>> = _mutationsState

    private val _actionState = MutableStateFlow<Resource<Unit>>(Resource.Empty)
    val actionState: StateFlow<Resource<Unit>> = _actionState

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentNoLoket: String?

    init {
        currentNoLoket = savedStateHandle["noLoket"]
        refreshData()
    }

    fun refreshData() {
        currentNoLoket?.let { noLoket ->
            loadLoketDetails(noLoket)
            loadMutations(noLoket)
        }
    }

    private fun loadLoketDetails(noLoket: String) {
        getLoketDetailUseCase(noLoket).onEach { result ->
            _loketDetailsState.value = result
        }.launchIn(viewModelScope)
    }

    private fun loadMutations(noLoket: String) {
        getMutationUseCase(noLoket).onEach { result ->
            _mutationsState.value = result
        }.launchIn(viewModelScope)
    }

    fun blockLoket() {
        currentNoLoket?.let { noLoket ->
            blockLoketUseCase(noLoket).onEach { result ->
                _actionState.value = result
                if (result is Resource.Success) {
                    viewModelScope.launch { _eventFlow.emit(UiEvent.ShowToast("Loket berhasil diblokir")) }
                    refreshData()
                }
            }.launchIn(viewModelScope)
        }
    }

    fun unblockLoket() {
        currentNoLoket?.let { noLoket ->
            unblockLoketUseCase(noLoket).onEach { result ->
                _actionState.value = result
                if (result is Resource.Success) {
                    viewModelScope.launch { _eventFlow.emit(UiEvent.ShowToast("Blokir loket berhasil dibuka")) }
                    refreshData()
                }
            }.launchIn(viewModelScope)
        }
    }

    fun clearAllFlags() {
        currentNoLoket?.let { noLoket ->
            clearAllFlagsUseCase(noLoket).onEach { result ->
                _actionState.value = result
                if (result is Resource.Success) {
                    viewModelScope.launch { _eventFlow.emit(UiEvent.ShowToast("Semua tanda berhasil dihapus")) }
                    refreshData()
                }
            }.launchIn(viewModelScope)
        }
    }

    fun onActionConsumed() {
        _actionState.value = Resource.Empty
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
    }
}
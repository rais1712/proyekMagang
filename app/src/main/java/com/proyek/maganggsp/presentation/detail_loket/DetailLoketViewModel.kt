package com.proyek.maganggsp.presentation.detail_loket

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
    private val flagMutationUseCase: FlagMutationUseCase,
    private val clearAllFlagsUseCase: ClearAllFlagsUseCase, // <<< Tambahkan ini
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    // ... (Semua StateFlow tetap sama)
    private val _loketDetailsState = MutableStateFlow<Resource<Loket>>(Resource.Empty())
    val loketDetailsState: StateFlow<Resource<Loket>> = _loketDetailsState

    private val _mutationsState = MutableStateFlow<Resource<List<Mutasi>>>(Resource.Empty())
    val mutationsState: StateFlow<Resource<List<Mutasi>>> = _mutationsState

    // ... (ActionState dan EventFlow tetap sama)
    private val _actionState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val actionState: StateFlow<Resource<Unit>> = _actionState

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()


    private var currentPhoneNumber: String? = null
    private var currentLoketId: String? = null

    init {
        savedStateHandle.get<String>("phone_number")?.let { phoneNumber ->
            currentPhoneNumber = phoneNumber
            loadAllData(phoneNumber)
        }
    }

    fun refreshData() {
        currentPhoneNumber?.let {
            // Reset state sebelum memuat ulang
            _loketDetailsState.value = Resource.Empty()
            _mutationsState.value = Resource.Empty()
            loadAllData(it)
        }
    }

    // --- PERBAIKAN: PENGAMBILAN DATA YANG LEBIH BERSIH ---
    private fun loadAllData(phoneNumber: String) {
        viewModelScope.launch {
            // Panggil use case untuk mendapatkan detail loket
            getLoketDetailUseCase(phoneNumber).collect { result ->
                _loketDetailsState.value = result
                // Jika sukses mendapatkan detail, ambil ID-nya dan panggil use case mutasi
                if (result is Resource.Success) {
                    result.data?.id?.let { loketId ->
                        currentLoketId = loketId
                        // Panggil fungsi terpisah untuk memuat mutasi
                        loadMutations(loketId)
                    }
                }
            }
        }
    }

    // Fungsi terpisah untuk memuat mutasi agar lebih rapi
    private fun loadMutations(loketId: String) {
        getMutationUseCase(loketId).onEach { mutationResult ->
            _mutationsState.value = mutationResult
        }.launchIn(viewModelScope)
    }

    // ... (Fungsi untuk block, unblock, dan flag tetap sama)
    fun blockLoket() {
        currentLoketId?.let { id ->
            blockLoketUseCase(id).onEach { result ->
                _actionState.value = result
                if (result is Resource.Success) {
                    viewModelScope.launch {
                        _eventFlow.emit(UiEvent.ShowToast("Loket berhasil diblokir"))
                    }
                    refreshData()
                }
            }.launchIn(viewModelScope)
        }
    }

    fun unblockLoket() {
        currentLoketId?.let { id ->
            unblockLoketUseCase(id).onEach { result ->
                _actionState.value = result
                if (result is Resource.Success) {
                    viewModelScope.launch {
                        _eventFlow.emit(UiEvent.ShowToast("Blokir loket berhasil dibuka"))
                    }
                    refreshData()
                }
            }.launchIn(viewModelScope)
        }
    }

    fun flagMutation(mutationId: String) {
        flagMutationUseCase(mutationId).onEach { result ->
            if (result is Resource.Success) {
                viewModelScope.launch {
                    _eventFlow.emit(UiEvent.ShowToast("Mutasi berhasil ditandai"))
                }
                refreshData()
            } else if (result is Resource.Error) {
                viewModelScope.launch {
                    _eventFlow.emit(UiEvent.ShowToast(result.message ?: "Gagal menandai mutasi"))
                }
            }
        }.launchIn(viewModelScope)
    }

    fun clearAllFlags() {
        currentLoketId?.let { id ->
            clearAllFlagsUseCase(id).onEach { result ->
                _actionState.value = result
                if (result is Resource.Success) {
                    viewModelScope.launch {
                        _eventFlow.emit(UiEvent.ShowToast("Semua tanda berhasil dihapus"))
                    }
                    refreshData() // Muat ulang data untuk melihat perubahan
                }
            }.launchIn(viewModelScope)
        }
    }



    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
    }
}
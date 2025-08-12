package com.proyek.maganggsp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.usecase.auth.GetAdminProfileUseCase
import com.proyek.maganggsp.domain.usecase.history.GetRecentHistoryUseCase
import com.proyek.maganggsp.domain.usecase.loket.SearchLoketUseCase
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAdminProfileUseCase: GetAdminProfileUseCase,
    private val getRecentHistoryUseCase: GetRecentHistoryUseCase,
    private val searchLoketUseCase: SearchLoketUseCase
) : ViewModel() {

    private val _adminProfileState = MutableStateFlow<Admin?>(null)
    val adminProfileState = _adminProfileState.asStateFlow()

    // --- MENGGUNAKAN SATU STATEFLOW UNTUK UI ---
    private val _uiState = MutableStateFlow<Resource<List<Loket>>>(Resource.Empty())
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null

    // Menyimpan riwayat terakhir untuk dikembalikan saat pencarian dibersihkan
    private var lastRecentHistory: List<Loket> = emptyList()

    init {
        loadAdminProfile()
        loadRecentHistory()
    }

    fun loadAdminProfile() {
        viewModelScope.launch {
            _adminProfileState.value = getAdminProfileUseCase()
        }
    }

    fun loadRecentHistory() {
        getRecentHistoryUseCase().onEach { result ->
            if (result is Resource.Success) {
                lastRecentHistory = result.data ?: emptyList()
            }
            _uiState.value = result
        }.launchIn(viewModelScope)
    }

    fun searchLoket(query: String) {
        searchJob?.cancel() // Batalkan job pencarian sebelumnya
        searchJob = viewModelScope.launch {
            delay(500L) // Memberi jeda agar tidak mencari setiap kali user mengetik
            if (query.isBlank()) {
                clearSearch()
            } else {
                searchLoketUseCase(query).onEach { result ->
                    _uiState.value = result
                }.launchIn(viewModelScope)
            }
        }
    }

    fun clearSearch() {
        // Kembalikan UI ke state riwayat terakhir
        _uiState.value = Resource.Success(lastRecentHistory)
    }

    fun refresh() {
        loadRecentHistory()
    }
}
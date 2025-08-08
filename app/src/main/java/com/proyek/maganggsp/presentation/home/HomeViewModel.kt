package com.proyek.maganggsp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.usecase.auth.GetAdminProfileUseCase
import com.proyek.maganggsp.domain.usecase.history.GetRecentHistoryUseCase
import com.proyek.maganggsp.domain.usecase.loket.SearchLoketUseCase
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.Resource.Empty
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@OptIn(FlowPreview::class)
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getRecentHistoryUseCase: GetRecentHistoryUseCase,
    private val searchLoketUseCase: SearchLoketUseCase,
    private val getAdminProfileUseCase: GetAdminProfileUseCase // <<< Tambahkan ini
) : ViewModel() {

    // State untuk Riwayat Terakhir
    private val _recentHistoryState = MutableStateFlow<Resource<List<Loket>>>(Empty())
    val recentHistoryState: StateFlow<Resource<List<Loket>>> = _recentHistoryState

    // State untuk Hasil Pencarian
    private val _searchResultState = MutableStateFlow<Resource<List<Loket>>>(Empty())
    val searchResultState: StateFlow<Resource<List<Loket>>> = _searchResultState

    // <<< State baru untuk Profil Admin >>>
    private val _adminProfileState = MutableStateFlow<Admin?>(null)
    val adminProfileState: StateFlow<Admin?> = _adminProfileState

    // State untuk query pencarian
    private val _searchQuery = MutableStateFlow("")

    init {
        loadAdminProfile() // Panggil fungsi ini
        getRecentHistory()
        observeSearchQuery()
    }

    // <<< Fungsi baru untuk memuat profil admin >>>
    private fun loadAdminProfile() {
        _adminProfileState.value = getAdminProfileUseCase()
    }

    private fun getRecentHistory() {
        getRecentHistoryUseCase().onEach { result ->
            _recentHistoryState.value = result
        }.launchIn(viewModelScope)
    }

    fun refreshRecentHistory() {
        viewModelScope.launch {
            _recentHistoryState.value = Resource.Loading()
            getRecentHistory()
        }
    }

    private fun observeSearchQuery() {
        _searchQuery
            .debounce(500L)
            .onEach { query ->
                if (query.isEmpty()) {
                    _searchResultState.value = Empty()
                } else {
                    _searchResultState.value = Resource.Loading()
                    searchLoketUseCase(query).onEach { result ->
                        _searchResultState.value = result
                    }.launchIn(viewModelScope)
                }
            }
            .launchIn(viewModelScope)
    }

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }
}
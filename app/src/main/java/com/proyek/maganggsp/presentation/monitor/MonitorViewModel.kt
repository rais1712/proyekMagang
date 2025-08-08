package com.proyek.maganggsp.presentation.monitor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.usecase.loket.GetBlockedLoketsUseCase
import com.proyek.maganggsp.domain.usecase.loket.GetFlaggedLoketsUseCase
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class MonitorViewModel @Inject constructor(
    private val getFlaggedLoketsUseCase: GetFlaggedLoketsUseCase,
    private val getBlockedLoketsUseCase: GetBlockedLoketsUseCase
) : ViewModel() {

    // State untuk daftar loket yang ditandai (Dipantau)
    private val _flaggedLoketsState = MutableStateFlow<Resource<List<Loket>>>(Resource.Empty())
    val flaggedLoketsState: StateFlow<Resource<List<Loket>>> = _flaggedLoketsState

    // State untuk daftar loket yang diblokir
    private val _blockedLoketsState = MutableStateFlow<Resource<List<Loket>>>(Resource.Empty())
    val blockedLoketsState: StateFlow<Resource<List<Loket>>> = _blockedLoketsState

    init {
        loadFlaggedLokets()
        loadBlockedLokets()
    }

    fun loadFlaggedLokets() {
        getFlaggedLoketsUseCase().onEach { result ->
            _flaggedLoketsState.value = result
        }.launchIn(viewModelScope)
    }

    fun loadBlockedLokets() {
        getBlockedLoketsUseCase().onEach { result ->
            _blockedLoketsState.value = result
        }.launchIn(viewModelScope)
    }

    /**
     * Refresh flagged lokets data
     * Dipanggil saat user melakukan pull-to-refresh atau manual refresh
     */
    fun refreshFlaggedLokets() {
        loadFlaggedLokets()
    }

    /**
     * Refresh blocked lokets data
     * Dipanggil saat user melakukan pull-to-refresh atau manual refresh
     */
    fun refreshBlockedLokets() {
        loadBlockedLokets()
    }

    /**
     * Refresh both flagged and blocked lokets data
     * Berguna untuk refresh keseluruhan data monitor
     */
    fun refreshAll() {
        refreshFlaggedLokets()
        refreshBlockedLokets()
    }
}
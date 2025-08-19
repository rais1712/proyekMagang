package com.proyek.maganggsp.presentation.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.usecase.history.GetFullHistoryUseCase
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val getFullHistoryUseCase: GetFullHistoryUseCase
) : ViewModel() {

    // FIXED: Gunakan Resource.Empty sebagai object
    private val _historyState = MutableStateFlow<Resource<List<Loket>>>(Resource.Empty)
    val historyState: StateFlow<Resource<List<Loket>>> = _historyState

    init {
        loadFullHistory()
    }

    private fun loadFullHistory() {
        getFullHistoryUseCase().onEach { result ->
            _historyState.value = result
        }.launchIn(viewModelScope)
    }
}
package com.proyek.maganggsp.view.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch

abstract class BaseViewModel : ViewModel() {
    private val _error = MutableSharedFlow<String>()
    val error: SharedFlow<String> = _error

    private val _loading = MutableSharedFlow<Boolean>()
    val loading: SharedFlow<Boolean> = _loading

    protected fun showError(message: String) {
        viewModelScope.launch {
            _error.emit(message)
        }
    }

    protected fun showLoading(isLoading: Boolean) {
        viewModelScope.launch {
            _loading.emit(isLoading)
        }
    }
}

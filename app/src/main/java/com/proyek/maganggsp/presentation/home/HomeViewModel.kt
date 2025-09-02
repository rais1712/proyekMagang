// File: app/src/main/java/com/proyek/maganggsp/presentation/home/HomeViewModel.kt
package com.proyek.maganggsp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.usecase.auth.GetAdminProfileUseCase
import com.proyek.maganggsp.domain.usecase.profile.GetProfileUseCase
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val getAdminProfileUseCase: GetAdminProfileUseCase,
    private val getProfileUseCase: GetProfileUseCase
) : ViewModel() {

    companion object {
        private const val DEFAULT_PPID = "PIDLKTD0025blok"
    }

    private val _adminProfileState = MutableStateFlow<Admin?>(null)
    val adminProfileState = _adminProfileState.asStateFlow()

    private val _uiState = MutableStateFlow<Resource<List<Receipt>>>(Resource.Empty)
    val uiState = _uiState.asStateFlow()

    init {
        loadAdminProfile()
        loadInitialData()
    }

    private fun loadAdminProfile() {
        try {
            val admin = getAdminProfileUseCase()
            _adminProfileState.value = admin
        } catch (e: Exception) {
            // Handle error silently for now
        }
    }

    private fun loadInitialData() {
        getProfileUseCase(DEFAULT_PPID).onEach { result ->
            // Convert single Receipt to List for compatibility
            _uiState.value = when (result) {
                is Resource.Success -> Resource.Success(listOf(result.data))
                is Resource.Error -> Resource.Error(result.exception)
                is Resource.Loading -> Resource.Loading()
                is Resource.Empty -> Resource.Empty
            }
        }.launchIn(viewModelScope)
    }

    fun searchReceipts(query: String) {
        if (query.isBlank()) {
            loadInitialData()
            return
        }

        if (query.length < 3) {
            _uiState.value = Resource.Success(emptyList())
            return
        }

        // For now, show empty search results
        _uiState.value = Resource.Success(emptyList())
    }

    fun refresh() {
        loadInitialData()
    }
}
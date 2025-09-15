// File: app/src/main/java/com/proyek/maganggsp/presentation/home/HomeViewModel.kt - UPDATED FOR LOKET
package com.proyek.maganggsp.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.model.LoketSearchHistory
import com.proyek.maganggsp.domain.usecase.auth.GetAdminProfileUseCase
import com.proyek.maganggsp.domain.usecase.loket.GetRecentLoketsUseCase
import com.proyek.maganggsp.domain.usecase.loket.SearchLoketHistoryUseCase
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
    private val getRecentLoketsUseCase: GetRecentLoketsUseCase,
    private val searchLoketHistoryUseCase: SearchLoketHistoryUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
    }

    private val _adminProfileState = MutableStateFlow<Admin?>(null)
    val adminProfileState = _adminProfileState.asStateFlow()

    private val _uiState = MutableStateFlow<Resource<List<LoketSearchHistory>>>(Resource.Loading())
    val uiState = _uiState.asStateFlow()

    init {
        Log.d(TAG, "REFACTORED HomeViewModel initialized for Loket search system")
        loadAdminProfile()
        loadRecentLokets()
    }

    private fun loadAdminProfile() {
        try {
            getAdminProfileUseCase()?.let { admin ->
                _adminProfileState.value = admin
                Log.d(TAG, "Admin profile loaded: ${admin.name}")
            } ?: run {
                Log.e(TAG, "Admin profile is null")
                setFallbackAdmin()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to load admin profile", e)
            setFallbackAdmin()
        }
    }

    private fun setFallbackAdmin() {
        _adminProfileState.value = Admin(
            name = "Admin User",
            email = "admin@gespay.com",
            token = "token"
        )
    }

    private fun loadRecentLokets() {
        Log.d(TAG, "Loading recent loket access history")

        getRecentLoketsUseCase().onEach { result ->
            _uiState.value = when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "Recent lokets loaded: ${result.data?.size ?: 0} items")
                    result
                }
                is Resource.Error -> {
                    Log.e(TAG, "Failed to load recent lokets: ${result.message}")
                    result
                }
                is Resource.Loading -> {
                    Log.d(TAG, "Loading recent lokets...")
                    result
                }
                is Resource.Empty -> {
                    Log.d(TAG, "No recent lokets found")
                    Resource.Success(emptyList())
                }
            }
        }.launchIn(viewModelScope)
    }

    fun searchLokets(query: String) {
        Log.d(TAG, "Search lokets with query: '$query'")

        searchLoketHistoryUseCase(query).onEach { result ->
            _uiState.value = when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "Search results: ${result.data?.size ?: 0} items")
                    result
                }
                is Resource.Error -> {
                    Log.e(TAG, "Search error: ${result.message}")
                    result
                }
                is Resource.Loading -> {
                    Log.d(TAG, "Searching...")
                    result
                }
                is Resource.Empty -> {
                    Log.d(TAG, "No search results")
                    Resource.Success(emptyList())
                }
            }
        }.launchIn(viewModelScope)
    }

    fun refresh() {
        Log.d(TAG, "Refreshing home data")
        loadAdminProfile()
        loadRecentLokets()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "HomeViewModel cleared")
    }
}


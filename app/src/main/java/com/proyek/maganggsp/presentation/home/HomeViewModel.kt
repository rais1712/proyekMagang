// File: app/src/main/java/com/proyek/maganggsp/presentation/home/HomeViewModel.kt
package com.proyek.maganggsp.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.usecase.auth.GetAdminProfileUseCase
import com.proyek.maganggsp.domain.usecase.history.GetRecentHistoryUseCase
import com.proyek.maganggsp.domain.usecase.loket.SearchLoketUseCase
import com.proyek.maganggsp.util.FeatureFlags
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

    private val _uiState = MutableStateFlow<Resource<List<Loket>>>(Resource.Empty)
    val uiState = _uiState.asStateFlow()

    private var searchJob: Job? = null
    private var lastRecentHistory: List<Loket> = emptyList()

    companion object {
        private const val TAG = "HomeViewModel"
    }

    init {
        // 🚩 FIXED: Corrected debug flag reference
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "=== HOME VIEWMODEL INITIALIZED ===")
            Log.d(TAG, "Real Data Loading: ${FeatureFlags.ENABLE_REAL_DATA_LOADING}")
            Log.d(TAG, "Search Enabled: ${FeatureFlags.ENABLE_SEARCH_LOKET}")
        }

        loadAdminProfile()
        loadInitialData()
    }

    private fun loadAdminProfile() {
        // 🚩 FEATURE FLAGS: Load admin profile only if enabled
        if (FeatureFlags.ENABLE_SESSION_MANAGEMENT) {
            try {
                val admin = getAdminProfileUseCase()
                _adminProfileState.value = admin

                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.d(TAG, "🚩 Admin profile loaded: ${admin?.name ?: "NULL"}")
                }
            } catch (e: Exception) {
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.e(TAG, "🚩 Failed to load admin profile", e)
                }
            }
        }
    }

    private fun loadInitialData() {
        // 🚩 FEATURE FLAGS: Load recent history only if enabled
        if (FeatureFlags.ENABLE_RECENT_HISTORY && FeatureFlags.ENABLE_REAL_DATA_LOADING) {
            getRecentHistoryUseCase().onEach { result ->
                _uiState.value = result

                // Cache successful results for search fallback
                if (result is Resource.Success) {
                    lastRecentHistory = result.data ?: emptyList()

                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.d(TAG, "🚩 Recent history loaded: ${lastRecentHistory.size} items")
                    }
                }
            }.launchIn(viewModelScope)
        } else {
            // Show empty state when features disabled
            _uiState.value = Resource.Success(emptyList())

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Recent history loading disabled by feature flags")
            }
        }
    }

    fun searchLoket(query: String) {
        // 🚩 FEATURE FLAGS: Search only if enabled
        if (!FeatureFlags.ENABLE_SEARCH_LOKET) {
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Search blocked by feature flag")
            }
            return
        }

        // Cancel previous search
        searchJob?.cancel()

        if (query.isBlank()) {
            // Return to recent history when query is empty
            _uiState.value = Resource.Success(lastRecentHistory)

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Query cleared, showing recent history")
            }
            return
        }

        if (query.length < 3) {
            // Show hint for short queries
            _uiState.value = Resource.Success(emptyList())

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Query too short: '$query'")
            }
            return
        }

        // 🚩 FEATURE FLAGS: Only perform real search if real data loading enabled
        if (FeatureFlags.ENABLE_REAL_DATA_LOADING) {
            searchJob = searchLoketUseCase(query).onEach { result ->
                _uiState.value = result

                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    when (result) {
                        is Resource.Success -> Log.d(TAG, "🚩 Search success: ${result.data?.size ?: 0} results")
                        is Resource.Error -> Log.e(TAG, "🚩 Search error: ${result.message}")
                        is Resource.Loading -> Log.d(TAG, "🚩 Search loading...")
                        else -> Log.d(TAG, "🚩 Search state: ${result::class.simpleName}")
                    }
                }
            }.launchIn(viewModelScope)
        } else {
            // Mock search behavior - just show empty results
            _uiState.value = Resource.Success(emptyList())

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Real data loading disabled - showing empty search results")
            }
        }
    }

    fun refresh() {
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 Refresh triggered")
        }

        // Cancel any ongoing search
        searchJob?.cancel()

        // Reload initial data
        loadInitialData()
    }

    /**
     * 🚩 SURGICAL CUTTING: Simplified state management
     * Only essential functions for core login-logout flow
     */
    fun getCurrentAdmin(): Admin? = _adminProfileState.value

    fun isSearchMode(): Boolean {
        // Simple check - if we have empty recent history cache, we're probably in search mode
        return lastRecentHistory.isEmpty() && (_uiState.value as? Resource.Success)?.data?.isNotEmpty() == true
    }

    override fun onCleared() {
        super.onCleared()

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 HomeViewModel cleared")
        }

        searchJob?.cancel()
    }
}
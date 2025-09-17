// File: app/src/main/java/com/proyek/maganggsp/presentation/home/HomeViewModel.kt - SEARCH BY PPID
package com.proyek.maganggsp.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.usecase.auth.GetAdminProfileUseCase
import com.proyek.maganggsp.domain.usecase.auth.LogoutUseCase
import com.proyek.maganggsp.domain.usecase.loket.GetRecentLoketsUseCase
import com.proyek.maganggsp.domain.usecase.loket.SearchLoketUseCase
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UPDATED: HomeViewModel dengan search by PPID functionality
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchLoketUseCase: SearchLoketUseCase,
    private val getRecentLoketsUseCase: GetRecentLoketsUseCase,
    private val getAdminProfileUseCase: GetAdminProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val SEARCH_DEBOUNCE_DELAY = 600L
    }

    // Search state management
    private val _searchResults = MutableStateFlow<Resource<List<Loket>>>(Resource.Empty)
    val searchResults: StateFlow<Resource<List<Loket>>> = _searchResults.asStateFlow()

    private val _recentLokets = MutableStateFlow<Resource<List<Loket>>>(Resource.Empty)
    val recentLokets: StateFlow<Resource<List<Loket>>> = _recentLokets.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    init {
        loadRecentLokets()
    }

    /**
     * UPDATED: Search loket by PPID dengan debounce
     */
    fun searchLoket(ppid: String) {
        // Cancel previous search job
        searchJob?.cancel()

        if (ppid.isBlank()) {
            clearSearch()
            return
        }

        searchJob = viewModelScope.launch {
            try {
                _isSearching.value = true

                // Debounce search to avoid too many operations
                delay(SEARCH_DEBOUNCE_DELAY)

                Log.d(TAG, "Starting search for PPID: $ppid")

                // Quick validation
                val validationResult = searchLoketUseCase.validateQuick(ppid)
                if (validationResult.isError) {
                    _searchResults.value = Resource.Error(
                        com.proyek.maganggsp.util.exceptions.AppException.ValidationException(
                            validationResult.message
                        )
                    )
                    return@launch
                }

                // Perform search (local cache + potential direct API if valid PPID)
                searchLoketUseCase(ppid).collect { resource ->
                    _searchResults.value = resource

                    when (resource) {
                        is Resource.Success -> {
                            Log.d(TAG, "Search successful: ${resource.data.size} results for PPID pattern")
                            AppUtils.logInfo(TAG, "Found ${resource.data.size} lokets for PPID: $ppid")
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "Search error: ${resource.exception.message}")
                            AppUtils.logError(TAG, "PPID search failed", resource.exception)
                        }
                        is Resource.Empty -> {
                            Log.d(TAG, "No results found for PPID: $ppid")
                        }
                        is Resource.Loading -> {
                            Log.d(TAG, "PPID search loading...")
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Search error", e)
                _searchResults.value = Resource.Error(
                    com.proyek.maganggsp.util.exceptions.AppException.UnknownException(
                        "Pencarian gagal: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * Clear search and show recent lokets
     */
    fun clearSearch() {
        searchJob?.cancel()
        _isSearching.value = false
        _searchResults.value = Resource.Empty
        loadRecentLokets()
    }

    /**
     * Load recent accessed lokets
     */
    fun loadRecentLokets() {
        if (_isSearching.value) return

        viewModelScope.launch {
            try {
                Log.d(TAG, "Loading recent lokets")

                getRecentLoketsUseCase().collect { resource ->
                    _recentLokets.value = resource

                    when (resource) {
                        is Resource.Success -> {
                            Log.d(TAG, "Recent lokets loaded: ${resource.data.size}")
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "Recent lokets error: ${resource.exception.message}")
                        }
                        is Resource.Empty -> {
                            Log.d(TAG, "No recent lokets available")
                        }
                        is Resource.Loading -> {
                            Log.d(TAG, "Loading recent lokets...")
                        }
                    }
                }

            } catch (e: Exception) {
                Log.e(TAG, "Recent lokets error", e)
                _recentLokets.value = Resource.Error(
                    com.proyek.maganggsp.util.exceptions.AppException.UnknownException(
                        "Gagal memuat riwayat: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * Get current admin profile
     */
    fun getAdminProfile(): Admin? {
        return try {
            getAdminProfileUseCase()
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get admin profile", e)
            null
        }
    }

    /**
     * Logout user
     */
    fun logout() {
        viewModelScope.launch {
            try {
                Log.d(TAG, "Starting logout process")
                logoutUseCase().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            Log.d(TAG, "Logout successful")
                            AppUtils.logInfo(TAG, "User logged out successfully")
                        }
                        is Resource.Error -> {
                            Log.e(TAG, "Logout error: ${resource.exception.message}")
                            AppUtils.logError(TAG, "Logout failed", resource.exception)
                        }
                        is Resource.Loading -> {
                            Log.d(TAG, "Logout in progress...")
                        }
                        is Resource.Empty -> { /* Not applicable for logout */ }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Logout error", e)
                AppUtils.logError(TAG, "Logout exception", e)
            }
        }
    }

    /**
     * Refresh data (pull to refresh)
     */
    fun refresh() {
        if (_isSearching.value) {
            // If currently searching, don't refresh recent - maintain search state
            Log.d(TAG, "Refresh called during search - ignoring")
        } else {
            loadRecentLokets()
        }
    }

    /**
     * UPDATED: Get PPID format examples untuk UI hints
     */
    fun getPpidFormatExamples(): List<String> {
        return searchLoketUseCase.getPpidFormatExamples()
    }

    /**
     * Direct PPID access - jika user input exact PPID, langsung coba load
     */
    fun directPpidAccess(ppid: String) {
        if (searchLoketUseCase.validateQuick(ppid).isValid) {
            // If valid PPID, user probably wants direct access
            // This will trigger search which may include direct API call
            searchLoket(ppid)
        }
    }

    /**
     * Debug info
     */
    fun getDebugInfo(): String {
        val adminProfile = getAdminProfile()
        return """
        HomeViewModel Debug Info:
        - Admin: ${adminProfile?.name ?: "Not found"}
        - Email: ${adminProfile?.email ?: "Not found"}
        - Is Searching: ${_isSearching.value}
        - Search Results: ${(_searchResults.value as? Resource.Success)?.data?.size ?: "N/A"}
        - Recent Lokets: ${(_recentLokets.value as? Resource.Success)?.data?.size ?: "N/A"}
        - Search Job Active: ${searchJob?.isActive ?: false}
        - Search Mode: PPID-based
        """.trimIndent()
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        Log.d(TAG, "ViewModel cleared")
    }
}
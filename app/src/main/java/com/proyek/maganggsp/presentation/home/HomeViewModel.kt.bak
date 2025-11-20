// File: app/src/main/java/com/proyek/maganggsp/presentation/home/HomeViewModel.kt - UPDATED MODULAR
package com.proyek.maganggsp.presentation.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.usecase.auth.GetAdminProfileUseCase
import com.proyek.maganggsp.domain.usecase.auth.LogoutUseCase
import com.proyek.maganggsp.domain.usecase.profile.GetRecentProfilesUseCase
import com.proyek.maganggsp.domain.usecase.profile.SearchProfilesUseCase
import com.proyek.maganggsp.util.LoggingUtils
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * MODULAR: HomeViewModel using separate modular use cases
 * Focused on Receipt display and PPID search
 */
@HiltViewModel
class HomeViewModel @Inject constructor(
    private val searchProfilesUseCase: SearchProfilesUseCase,
    private val getRecentProfilesUseCase: GetRecentProfilesUseCase,
    private val getAdminProfileUseCase: GetAdminProfileUseCase,
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val SEARCH_DEBOUNCE_DELAY = 500L
    }

    // MODULAR STATE MANAGEMENT
    private val _searchResults = MutableStateFlow<Resource<List<Receipt>>>(Resource.Empty())
    val searchResults: StateFlow<Resource<List<Receipt>>> = _searchResults.asStateFlow()

    private val _recentReceipts = MutableStateFlow<Resource<List<Receipt>>>(Resource.Empty())
    val recentReceipts: StateFlow<Resource<List<Receipt>>> = _recentReceipts.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    init {
        LoggingUtils.logInfo(TAG, "MODULAR HomeViewModel initialized with separate use cases")
        loadRecentReceipts()
    }

    /**
     * Search receipts by PPID with debounce
     */
    fun searchReceipts(ppid: String) {
        searchJob?.cancel()

        if (ppid.isBlank()) {
            clearSearch()
            return
        }

        searchJob = viewModelScope.launch {
            try {
                _isSearching.value = true
                delay(SEARCH_DEBOUNCE_DELAY)

                LoggingUtils.logDebug(TAG, "Starting PPID search: $ppid")

                // Quick validation
                val validationResult = searchProfilesUseCase.validatePpid(ppid)
                if (!validationResult.isValid && ppid.length >= 5) {
                    _searchResults.value = Resource.Error(
                        com.proyek.maganggsp.util.exceptions.AppException.ValidationException(
                            validationResult.message
                        )
                    )
                    return@launch
                }

                // Perform search using modular use case
                searchProfilesUseCase(ppid).collect { resource ->
                    _searchResults.value = resource

                    when (resource) {
                        is Resource.Success -> {
                            LoggingUtils.logInfo(TAG, "Search successful: ${resource.data.size} receipts found")
                        }
                        is Resource.Error -> {
                            LoggingUtils.logError(TAG, "Search error", resource.message)
                        }
                        is Resource.Empty() -> {
                            LoggingUtils.logDebug(TAG, "No results found for PPID: $ppid")
                        }
                        is Resource.Loading -> {
                            LoggingUtils.logDebug(TAG, "Searching receipts...")
                        }
                    }
                }

            } catch (e: Exception) {
                LoggingUtils.logError(TAG, "Search exception", e)
                _searchResults.value = Resource.Error(
                    com.proyek.maganggsp.util.exceptions.AppException.UnknownException(
                        "Pencarian gagal: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * Clear search and show recent receipts
     */
    fun clearSearch() {
        searchJob?.cancel()
        _isSearching.value = false
        _searchResults.value = Resource.Empty()
        loadRecentReceipts()
        LoggingUtils.logDebug(TAG, "Search cleared, showing recent receipts")
    }

    /**
     * Load recent receipts from history using modular use case
     */
    fun loadRecentReceipts() {
        if (_isSearching.value) return

        viewModelScope.launch {
            try {
                LoggingUtils.logDebug(TAG, "Loading recent receipts")

                getRecentProfilesUseCase().collect { resource ->
                    _recentReceipts.value = resource

                    when (resource) {
                        is Resource.Success -> {
                            LoggingUtils.logInfo(TAG, "Recent receipts loaded: ${resource.data.size}")
                        }
                        is Resource.Error -> {
                            LoggingUtils.logError(TAG, "Recent receipts error", resource.message)
                        }
                        is Resource.Empty() -> {
                            LoggingUtils.logDebug(TAG, "No recent receipts available")
                        }
                        is Resource.Loading -> {
                            LoggingUtils.logDebug(TAG, "Loading recent receipts...")
                        }
                    }
                }

            } catch (e: Exception) {
                LoggingUtils.logError(TAG, "Recent receipts exception", e)
                _recentReceipts.value = Resource.Error(
                    com.proyek.maganggsp.util.exceptions.AppException.UnknownException(
                        "Gagal memuat riwayat: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * Get admin profile for display
     */
    fun getAdminProfile(): Admin? {
        return try {
            val admin = getAdminProfileUseCase()
            LoggingUtils.logDebug(TAG, "Admin profile: ${admin?.name ?: "Not found"}")
            admin
        } catch (e: Exception) {
            LoggingUtils.logError(TAG, "Failed to get admin profile", e)
            null
        }
    }

    /**
     * Logout process using modular use case
     */
    fun logout() {
        viewModelScope.launch {
            try {
                LoggingUtils.logInfo(TAG, "Starting logout process")

                logoutUseCase().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            LoggingUtils.logInfo(TAG, "Logout successful")
                        }
                        is Resource.Error -> {
                            LoggingUtils.logError(TAG, "Logout error", resource.message)
                        }
                        is Resource.Loading -> {
                            LoggingUtils.logDebug(TAG, "Logout in progress...")
                        }
                        is Resource.Empty() -> { /* Not applicable */ }
                    }
                }
            } catch (e: Exception) {
                LoggingUtils.logError(TAG, "Logout exception", e)
            }
        }
    }

    /**
     * Refresh data (pull to refresh)
     */
    fun refresh() {
        if (_isSearching.value) {
            LoggingUtils.logDebug(TAG, "Refresh ignored during active search")
        } else {
            LoggingUtils.logInfo(TAG, "Refreshing recent receipts")
            loadRecentReceipts()
        }
    }

    /**
     * Direct PPID access for exact match
     */
    fun accessByPpid(ppid: String) {
        val validation = searchProfilesUseCase.validatePpid(ppid)
        if (validation.isValid) {
            LoggingUtils.logInfo(TAG, "Direct PPID access: $ppid")
            searchReceipts(ppid)
        } else {
            LoggingUtils.logDebug(TAG, "Invalid PPID format for direct access: $ppid")
        }
    }

    /**
     * Get PPID format examples for UI hints
     */
    fun getPpidFormatExamples(): List<String> {
        return searchProfilesUseCase.getPpidFormatExamples()
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        LoggingUtils.logInfo(TAG, "HomeViewModel cleared")
    }
}

// File: app/src/main/java/com/proyek/maganggsp/presentation/home/HomeViewModel.kt - RECEIPT FOCUSED
package com.proyek.maganggsp.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.usecase.auth.GetAdminProfileUseCase
import com.proyek.maganggsp.domain.usecase.auth.LogoutUseCase
import com.proyek.maganggsp.domain.usecase.profile.GetRecentProfilesUseCase
import com.proyek.maganggsp.domain.usecase.profile.SearchProfilesUseCase
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.AppUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * STREAMLINED: HomeViewModel focused on Receipt display and PPID search
 * Eliminates complex loket management, focuses on receipt data
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

    // STREAMLINED STATE MANAGEMENT
    private val _searchResults = MutableStateFlow<Resource<List<Receipt>>>(Resource.Empty)
    val searchResults: StateFlow<Resource<List<Receipt>>> = _searchResults.asStateFlow()

    private val _recentReceipts = MutableStateFlow<Resource<List<Receipt>>>(Resource.Empty)
    val recentReceipts: StateFlow<Resource<List<Receipt>>> = _recentReceipts.asStateFlow()

    private val _isSearching = MutableStateFlow(false)
    val isSearching: StateFlow<Boolean> = _isSearching.asStateFlow()

    private var searchJob: Job? = null

    init {
        AppUtils.logInfo(TAG, "HomeViewModel initialized with Receipt focus")
        loadRecentReceipts()
    }

    /**
     * STREAMLINED: Search receipts by PPID dengan debounce
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

                AppUtils.logDebug(TAG, "Starting PPID search: $ppid")

                // Quick validation
                val validationResult = AppUtils.validatePpidFormat(ppid)
                if (!validationResult.isValid && ppid.length >= 5) {
                    _searchResults.value = Resource.Error(
                        com.proyek.maganggsp.util.exceptions.AppException.ValidationException(
                            validationResult.message
                        )
                    )
                    return@launch
                }

                // Perform search
                searchProfilesUseCase(ppid).collect { resource ->
                    _searchResults.value = resource

                    when (resource) {
                        is Resource.Success -> {
                            AppUtils.logInfo(TAG, "Search successful: ${resource.data.size} receipts found")
                        }
                        is Resource.Error -> {
                            AppUtils.logError(TAG, "Search error", resource.exception)
                        }
                        is Resource.Empty -> {
                            AppUtils.logDebug(TAG, "No results found for PPID: $ppid")
                        }
                        is Resource.Loading -> {
                            AppUtils.logDebug(TAG, "Searching receipts...")
                        }
                    }
                }

            } catch (e: Exception) {
                AppUtils.logError(TAG, "Search exception", e)
                _searchResults.value = Resource.Error(
                    com.proyek.maganggsp.util.exceptions.AppException.UnknownException(
                        "Pencarian gagal: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * STREAMLINED: Clear search and show recent receipts
     */
    fun clearSearch() {
        searchJob?.cancel()
        _isSearching.value = false
        _searchResults.value = Resource.Empty
        loadRecentReceipts()
        AppUtils.logDebug(TAG, "Search cleared, showing recent receipts")
    }

    /**
     * STREAMLINED: Load recent receipts from history
     */
    fun loadRecentReceipts() {
        if (_isSearching.value) return

        viewModelScope.launch {
            try {
                AppUtils.logDebug(TAG, "Loading recent receipts")

                getRecentProfilesUseCase().collect { resource ->
                    _recentReceipts.value = resource

                    when (resource) {
                        is Resource.Success -> {
                            AppUtils.logInfo(TAG, "Recent receipts loaded: ${resource.data.size}")
                        }
                        is Resource.Error -> {
                            AppUtils.logError(TAG, "Recent receipts error", resource.exception)
                        }
                        is Resource.Empty -> {
                            AppUtils.logDebug(TAG, "No recent receipts available")
                        }
                        is Resource.Loading -> {
                            AppUtils.logDebug(TAG, "Loading recent receipts...")
                        }
                    }
                }

            } catch (e: Exception) {
                AppUtils.logError(TAG, "Recent receipts exception", e)
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
            AppUtils.logDebug(TAG, "Admin profile: ${admin?.name ?: "Not found"}")
            admin
        } catch (e: Exception) {
            AppUtils.logError(TAG, "Failed to get admin profile", e)
            null
        }
    }

    /**
     * STREAMLINED: Logout process
     */
    fun logout() {
        viewModelScope.launch {
            try {
                AppUtils.logInfo(TAG, "Starting logout process")

                logoutUseCase().collect { resource ->
                    when (resource) {
                        is Resource.Success -> {
                            AppUtils.logInfo(TAG, "Logout successful")
                        }
                        is Resource.Error -> {
                            AppUtils.logError(TAG, "Logout error", resource.exception)
                        }
                        is Resource.Loading -> {
                            AppUtils.logDebug(TAG, "Logout in progress...")
                        }
                        is Resource.Empty -> { /* Not applicable */ }
                    }
                }
            } catch (e: Exception) {
                AppUtils.logError(TAG, "Logout exception", e)
            }
        }
    }

    /**
     * Refresh data (pull to refresh)
     */
    fun refresh() {
        if (_isSearching.value) {
            AppUtils.logDebug(TAG, "Refresh ignored during active search")
        } else {
            AppUtils.logInfo(TAG, "Refreshing recent receipts")
            loadRecentReceipts()
        }
    }

    /**
     * Direct PPID access untuk exact match
     */
    fun accessByPpid(ppid: String) {
        val validation = AppUtils.validatePpidFormat(ppid)
        if (validation.isValid) {
            AppUtils.logInfo(TAG, "Direct PPID access: $ppid")
            searchReceipts(ppid)
        } else {
            AppUtils.logDebug(TAG, "Invalid PPID format for direct access: $ppid")
        }
    }

    /**
     * Get PPID format examples for UI hints
     */
    fun getPpidFormatExamples(): List<String> {
        return listOf(
            "PIDLKTD0025",
            "PIDLKTD0025blok",
            "PIDLKTD0030"
        )
    }

    /**
     * STREAMLINED: Get current state summary
     */
    fun getCurrentStateSummary(): String {
        val adminProfile = getAdminProfile()
        val searchResultsCount = (_searchResults.value as? Resource.Success)?.data?.size ?: 0
        val recentReceiptsCount = (_recentReceipts.value as? Resource.Success)?.data?.size ?: 0

        return """
        Home State Summary:
        - Admin: ${adminProfile?.name ?: "Not loaded"}
        - Is Searching: ${_isSearching.value}
        - Search Results: $searchResultsCount
        - Recent Receipts: $recentReceiptsCount
        - Search Job Active: ${searchJob?.isActive ?: false}
        """.trimIndent()
    }

    /**
     * STREAMLINED: Debug info
     */
    fun getDebugInfo(): String {
        return """
        ${getCurrentStateSummary()}
        
        Technical Details:
        - ViewModel: Receipt-focused HomeViewModel
        - Search Mode: PPID-based
        - Data Source: ProfileRepository
        - State Management: Streamlined StateFlow
        """.trimIndent()
    }

    override fun onCleared() {
        super.onCleared()
        searchJob?.cancel()
        AppUtils.logInfo(TAG, "HomeViewModel cleared")
    }
}
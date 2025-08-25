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
        if (FeatureFlags.ENABLE_DEBUG_LOGS) {
            Log.d(TAG, "=== HOME VIEWMODEL INITIALIZED ===")
            Log.d(TAG, "Admin Profile Enabled: ${FeatureFlags.ENABLE_ADMIN_PROFILE_DISPLAY}")
            Log.d(TAG, "Real Data Loading: ${FeatureFlags.ENABLE_REAL_DATA_LOADING}")
            Log.d(TAG, "API Calls Enabled: ${FeatureFlags.ENABLE_LOKET_API_CALLS}")
        }

        loadAdminProfile()
        loadInitialData()
    }

    private fun loadAdminProfile() {
        if (FeatureFlags.ENABLE_ADMIN_PROFILE
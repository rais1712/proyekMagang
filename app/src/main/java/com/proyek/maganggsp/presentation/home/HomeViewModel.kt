// File: app/src/main/java/com/proyek/maganggsp/presentation/home/HomeViewModel.kt - REFACTORED
package com.proyek.maganggsp.presentation.home

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.usecase.auth.GetAdminProfileUseCase
import com.proyek.maganggsp.domain.usecase.profile.GetProfileUseCase
import com.proyek.maganggsp.domain.usecase.profile.SearchProfilesUseCase
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
    private val getProfileUseCase: GetProfileUseCase,
    private val searchProfilesUseCase: SearchProfilesUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "HomeViewModel"
        private const val DEFAULT_PPID = "PIDLKTD0025blok" // Default PPID for initial data
    }

    private val _adminProfileState = MutableStateFlow<Admin?>(null)
    val adminProfileState = _adminProfileState.asStateFlow()

    private val _uiState = MutableStateFlow<Resource<List<Receipt>>>(Resource.Loading())
    val uiState = _uiState.asStateFlow()

    init {
        Log.d(TAG, "🔄 REFACTORED HomeViewModel initialized for Receipt data structure")
        loadAdminProfile()
        loadInitialData()
    }

    private fun loadAdminProfile() {
        try {
            getAdminProfileUseCase()?.let { admin ->
                _adminProfileState.value = admin
                Log.d(TAG, "👤 Admin profile loaded: ${admin.name}")
            } ?: run {
                Log.e(TAG, "❌ Admin profile is null")
                setFallbackAdmin()
            }
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load admin profile", e)
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

    private fun loadInitialData() {
        Log.d(TAG, "📡 Loading initial receipt data from /profiles/ppid/${DEFAULT_PPID}")

        try {
            getProfileUseCase(DEFAULT_PPID).onEach { result ->
                _uiState.value = when (result) {
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Profile data received: ${result.data?.refNumber}")
                        Resource.Success(listOfNotNull(result.data))
                    }
                    is Resource.Error -> {
                        Log.e(TAG, "❌ Profile data error: ${result.message}")
                        Resource.Error(result.message ?: "Unknown error occurred")
                    }
                    is Resource.Loading -> {
                        Log.d(TAG, "⏳ Loading profile data...")
                        Resource.Loading()
                    }
                    is Resource.Empty -> {
                        Log.d(TAG, "📋 Empty profile data")
                        Resource.Success(emptyList())
                    }
                }
            }.launchIn(viewModelScope)
        } catch (e: Exception) {
            Log.e(TAG, "❌ Failed to load initial data", e)
            _uiState.value = Resource.Error("Failed to load data: ${e.message}")
        }
    }

    fun searchReceipts(query: String) {
        Log.d(TAG, "🔍 Search receipts with query: '$query'")

        try {
            when {
                query.isBlank() -> {
                    Log.d(TAG, "📋 Empty query - loading initial data")
                    loadInitialData()
                }
                query.length < 3 -> {
                    Log.d(TAG, "⚠️ Query too short: ${query.length} chars")
                    _uiState.value = Resource.Success(emptyList())
                }
                else -> {
                    searchProfilesUseCase(query).onEach { result ->
                        _uiState.value = when (result) {
                            is Resource.Success -> {
                                Log.d(TAG, "✅ Search results: ${result.data?.size ?: 0} items")
                                result
                            }
                            is Resource.Error -> {
                                Log.e(TAG, "❌ Search error: ${result.message}")
                                Resource.Error(result.message ?: "Search failed")
                            }
                            is Resource.Loading -> {
                                Log.d(TAG, "⏳ Searching...")
                                Resource.Loading()
                            }
                            is Resource.Empty -> {
                                Log.d(TAG, "📋 No search results")
                                Resource.Success(emptyList())
                            }
                        }
                    }.launchIn(viewModelScope)
                }
            }
        }
    }

    fun refresh() {
        Log.d(TAG, "🔄 Refreshing receipt data")
        loadAdminProfile()
        loadInitialData()
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 HomeViewModel cleared")
    }
}
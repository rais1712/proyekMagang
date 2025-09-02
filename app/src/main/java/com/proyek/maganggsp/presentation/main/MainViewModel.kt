// File: app/src/main/java/com/proyek/maganggsp/presentation/main/MainViewModel.kt
package com.proyek.maganggsp.presentation.main

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.usecase.auth.GetAdminProfileUseCase
import com.proyek.maganggsp.domain.usecase.auth.IsLoggedInUseCase
import com.proyek.maganggsp.domain.usecase.auth.LogoutUseCase
import com.proyek.maganggsp.data.source.local.SessionManager
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val isLoggedInUseCase: IsLoggedInUseCase,
    private val getAdminProfileUseCase: GetAdminProfileUseCase,
    private val logoutUseCase: LogoutUseCase,
    private val sessionManager: SessionManager
) : ViewModel() {

    companion object {
        private const val TAG = "MainViewModel"
    }

    private val _sessionState = MutableStateFlow<Resource<Admin>>(Resource.Empty)
    val sessionState = _sessionState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 MainViewModel initialized")
        }
    }

    /**
     * 🚩 FEATURE FLAGS: Check session validity when MainActivity starts
     */
    fun checkSessionValidity() {
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 Checking session validity")
        }

        viewModelScope.launch {
            try {
                // 🚩 FEATURE FLAGS: Only check if session management enabled
                if (!FeatureFlags.ENABLE_SESSION_MANAGEMENT) {
                    // If session management disabled, create mock admin for testing
                    val mockAdmin = Admin(
                        name = "Admin Test",
                        email = "test@admin.com",
                        token = "mock_token"
                    )
                    _sessionState.value = Resource.Success(mockAdmin)

                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.w(TAG, "🚩 Session management disabled - using mock admin")
                    }
                    return@launch
                }

                if (!isLoggedInUseCase()) {
                    // Not logged in, emit error to trigger navigation to login
                    _sessionState.value = Resource.Error(
                        AppException.AuthenticationException("Session tidak valid")
                    )

                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.w(TAG, "🚩 User not logged in")
                    }
                    return@launch
                }

                // User is logged in, get profile
                val adminProfile = getAdminProfileUseCase()
                if (adminProfile != null) {
                    _sessionState.value = Resource.Success(adminProfile)

                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.d(TAG, "🚩 Session valid - Admin: ${adminProfile.name}")
                    }
                } else {
                    // Profile not found, session might be corrupted
                    _sessionState.value = Resource.Error(
                        AppException.AuthenticationException("Profil admin tidak ditemukan")
                    )

                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.e(TAG, "🚩 Admin profile not found despite valid session")
                    }
                }
            } catch (e: Exception) {
                _sessionState.value = Resource.Error(
                    AppException.UnknownException("Gagal memeriksa status login")
                )

                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.e(TAG, "🚩 Exception during session check", e)
                }
            }
        }
    }

    /**
     * 🚩 FEATURE FLAGS: Perform logout and clear session
     */
    fun logout() {
        if (!FeatureFlags.ENABLE_LOGOUT) {
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Logout blocked by feature flag")
            }
            return
        }

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 Starting logout process")
        }

        logoutUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _eventFlow.emit(UiEvent.ShowMessage("Logout berhasil"))
                    _eventFlow.emit(UiEvent.SessionExpired)

                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.d(TAG, "🚩 Logout successful")
                    }
                }
                is Resource.Error -> {
                    if (FeatureFlags.ENABLE_DETAILED_ERROR_MESSAGES) {
                        _eventFlow.emit(UiEvent.ShowMessage("Gagal logout: ${result.message}"))
                    } else {
                        _eventFlow.emit(UiEvent.ShowMessage("Gagal logout"))
                    }

                    // Even if logout API fails, clear local session
                    _eventFlow.emit(UiEvent.SessionExpired)

                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.w(TAG, "🚩 Logout API failed but clearing local session: ${result.message}")
                    }
                }
                is Resource.Loading -> {
                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.d(TAG, "🚩 Logout in progress...")
                    }
                }
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Get current admin profile for UI display
     */
    fun getCurrentAdmin(): Admin? {
        return (_sessionState.value as? Resource.Success)?.data
    }

    /**
     * Refresh session data
     */
    fun refreshSession() {
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 Refreshing session")
        }
        checkSessionValidity()
    }

    /**
     * 🚩 SURGICAL CUTTING: Get session debug info for troubleshooting
     */
    fun getSessionDebugInfo(): String {
        return if (FeatureFlags.ENABLE_SESSION_DEBUG_INFO) {
            try {
                val currentAdmin = getCurrentAdmin()
                val sessionDebug = sessionManager.debugSessionState()
                """
                Current Admin: ${currentAdmin?.name ?: "NULL"}
                Email: ${currentAdmin?.email ?: "NULL"}
                Token Exists: ${currentAdmin?.token?.isNotEmpty() ?: false}
                $sessionDebug
                """.trimIndent()
            } catch (e: Exception) {
                "Session Debug Error: ${e.message}"
            }
        } else {
            "Debug info disabled"
        }
    }

    /**
     * 🚩 FEATURE FLAGS: Check if user can access certain features
     */
    fun canAccessFeature(feature: String): Boolean {
        return when (feature.lowercase()) {
            "history" -> FeatureFlags.ENABLE_HISTORY_FRAGMENT
            "monitor" -> FeatureFlags.ENABLE_MONITOR_FRAGMENT
            "search" -> FeatureFlags.ENABLE_SEARCH_LOKET
            "detail" -> FeatureFlags.ENABLE_LOKET_DETAIL_VIEW
            "logout" -> FeatureFlags.ENABLE_LOGOUT
            else -> false
        }
    }

    /**
     * Force logout - for emergency situations
     */
    fun forceLogout() {
        viewModelScope.launch {
            try {
                sessionManager.clearSession()
                _eventFlow.emit(UiEvent.SessionExpired)

                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.w(TAG, "🚩 Force logout executed")
                }
            } catch (e: Exception) {
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.e(TAG, "🚩 Force logout failed", e)
                }
            }
        }
    }

    override fun onCleared() {
        super.onCleared()

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 MainViewModel cleared")
        }
    }

    sealed class UiEvent {
        object SessionExpired : UiEvent()
        data class ShowMessage(val message: String) : UiEvent()
    }
}
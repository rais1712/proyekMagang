// File: app/src/main/java/com/proyek/maganggsp/presentation/main/MainViewModel.kt - SIMPLIFIED
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

    private val _sessionState = MutableStateFlow<Resource<Admin>>(Resource.Empty())
    val sessionState = _sessionState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        Log.d(TAG, "🔄 SIMPLIFIED MainViewModel initialized - FeatureFlags removed")
    }

    fun checkSessionValidity() {
        Log.d(TAG, "🔍 Checking session validity")

        viewModelScope.launch {
            try {
                if (!isLoggedInUseCase()) {
                    // Not logged in, emit error to trigger navigation to login
                    _sessionState.value = Resource.Error(
                        AppException.AuthenticationException("Session not valid")
                    )
                    Log.w(TAG, "⚠️ User not logged in")
                    return@launch
                }

                // User is logged in, get profile
                val adminProfile = getAdminProfileUseCase()
                if (adminProfile != null) {
                    _sessionState.value = Resource.Success(adminProfile)
                    Log.d(TAG, "✅ Session valid - Admin: ${adminProfile.name}")
                } else {
                    // Profile not found, session might be corrupted
                    _sessionState.value = Resource.Error(
                        AppException.AuthenticationException("Admin profile not found")
                    )
                    Log.e(TAG, "❌ Admin profile not found despite valid session")
                }
            } catch (e: Exception) {
                _sessionState.value = Resource.Error(
                    AppException.UnknownException("Failed to check login status")
                )
                Log.e(TAG, "❌ Exception during session check", e)
            }
        }
    }

    fun logout() {
        Log.d(TAG, "🚪 Starting logout process")

        logoutUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _eventFlow.emit(UiEvent.ShowMessage("Logout successful"))
                    _eventFlow.emit(UiEvent.SessionExpired)
                    Log.d(TAG, "✅ Logout successful")
                }
                is Resource.Error -> {
                    _eventFlow.emit(UiEvent.ShowMessage("Logout failed: ${result.message}"))
                    // Even if logout API fails, clear local session
                    _eventFlow.emit(UiEvent.SessionExpired)
                    Log.w(TAG, "⚠️ Logout API failed but clearing local session: ${result.message}")
                }
                is Resource.Loading -> {
                    Log.d(TAG, "⏳ Logout in progress...")
                }
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    fun getCurrentAdmin(): Admin? {
        return (_sessionState.value as? Resource.Success)?.data
    }

    fun refreshSession() {
        Log.d(TAG, "🔄 Refreshing session")
        checkSessionValidity()
    }

    fun getSessionDebugInfo(): String {
        return try {
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
    }

    fun forceLogout() {
        viewModelScope.launch {
            try {
                sessionManager.clearSession()
                _eventFlow.emit(UiEvent.SessionExpired)
                Log.w(TAG, "⚠️ Force logout executed")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Force logout failed", e)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 MainViewModel cleared")
    }

    sealed class UiEvent {
        object SessionExpired : UiEvent()
        data class ShowMessage(val message: String) : UiEvent()
    }
}
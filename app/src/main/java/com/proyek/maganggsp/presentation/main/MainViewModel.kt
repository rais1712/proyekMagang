// File: app/src/main/java/com/proyek/maganggsp/presentation/MainViewModel.kt
package com.proyek.maganggsp.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.data.source.local.SessionManager
import com.proyek.maganggsp.domain.usecase.auth.IsLoggedInUseCase
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor(
    private val sessionManager: SessionManager,
    private val isLoggedInUseCase: IsLoggedInUseCase
) : ViewModel() {

    // Session state untuk monitoring validitas login
    private val _sessionState = MutableStateFlow<Resource<Unit>>(Resource.Empty)
    val sessionState: StateFlow<Resource<Unit>> = _sessionState

    // Event flow untuk one-time events (seperti navigation, toast)
    private val _eventFlow = MutableSharedFlow<UiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val eventFlow = _eventFlow.asSharedFlow()

    // Current navigation destination tracking
    private val _currentDestination = MutableStateFlow<Int?>(null)
    val currentDestination: StateFlow<Int?> = _currentDestination

    /**
     * Check session validity saat app dibuka
     */
    fun checkSessionValidity() {
        viewModelScope.launch {
            try {
                _sessionState.value = Resource.Loading()

                val isValid = isLoggedInUseCase()

                if (isValid) {
                    _sessionState.value = Resource.Success(Unit)
                } else {
                    _sessionState.value = Resource.Error(
                        AppException.AuthenticationException("Sesi telah berakhir")
                    )
                    emitEvent(UiEvent.SessionExpired)
                }
            } catch (e: Exception) {
                _sessionState.value = Resource.Error(
                    AppException.UnknownException("Gagal memeriksa sesi login", e)
                )
                emitEvent(UiEvent.SessionExpired)
            }
        }
    }

    /**
     * Force refresh session check
     */
    fun refreshSession() {
        checkSessionValidity()
    }

    /**
     * Update current navigation destination
     */
    fun updateCurrentDestination(destinationId: Int) {
        _currentDestination.value = destinationId
    }

    /**
     * Get remaining session time for display
     */
    fun getRemainingSessionTime(): String {
        return try {
            val remainingMinutes = sessionManager.getRemainingSessionTimeMinutes()
            when {
                remainingMinutes <= 0 -> "Sesi berakhir"
                remainingMinutes < 60 -> "${remainingMinutes}m tersisa"
                else -> "${remainingMinutes / 60}j ${remainingMinutes % 60}m tersisa"
            }
        } catch (e: Exception) {
            "Tidak diketahui"
        }
    }

    /**
     * Check if session is expiring soon (less than 10 minutes)
     */
    fun isSessionExpiringSoon(): Boolean {
        return try {
            sessionManager.getRemainingSessionTimeMinutes() < 10
        } catch (e: Exception) {
            false
        }
    }

    /**
     * Show session expiry warning
     */
    fun showSessionExpiryWarning() {
        if (isSessionExpiringSoon()) {
            emitEvent(UiEvent.ShowMessage("Sesi Anda akan berakhir dalam ${getRemainingSessionTime()}"))
        }
    }

    /**
     * Handle when user confirms logout
     */
    fun confirmLogout() {
        viewModelScope.launch {
            try {
                sessionManager.clearSession()
                emitEvent(UiEvent.SessionExpired)
            } catch (e: Exception) {
                emitEvent(UiEvent.ShowMessage("Gagal logout: ${e.message}"))
            }
        }
    }

    /**
     * Get session debug info (for development)
     */
    fun getSessionDebugInfo(): String {
        return sessionManager.debugSessionState()
    }

    /**
     * Private helper to emit events safely
     */
    private fun emitEvent(event: UiEvent) {
        viewModelScope.launch {
            _eventFlow.emit(event)
        }
    }

    /**
     * Sealed class untuk UI events
     */
    sealed class UiEvent {
        object SessionExpired : UiEvent()
        data class ShowMessage(val message: String) : UiEvent()
        data class NavigateToLogin(val clearBackStack: Boolean = true) : UiEvent()
        data class ShowSessionWarning(val remainingTime: String) : UiEvent()
    }

    /**
     * Clean up when ViewModel destroyed
     */
    override fun onCleared() {
        super.onCleared()
        // Clean up any ongoing operations if needed
    }
}
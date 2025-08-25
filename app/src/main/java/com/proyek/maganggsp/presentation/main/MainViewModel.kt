// File: app/src/main/java/com/proyek/maganggsp/presentation/main/MainViewModel.kt
package com.proyek.maganggsp.presentation.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.usecase.auth.GetAdminProfileUseCase
import com.proyek.maganggsp.domain.usecase.auth.IsLoggedInUseCase
import com.proyek.maganggsp.domain.usecase.auth.LogoutUseCase
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
    private val logoutUseCase: LogoutUseCase
) : ViewModel() {

    private val _sessionState = MutableStateFlow<Resource<Admin>>(Resource.Empty)
    val sessionState = _sessionState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    /**
     * Check session validity when MainActivity starts
     */
    fun checkSessionValidity() {
        viewModelScope.launch {
            try {
                if (!isLoggedInUseCase()) {
                    // Not logged in, emit error to trigger navigation to login
                    _sessionState.value = Resource.Error(
                        AppException.AuthenticationException("Session tidak valid")
                    )
                    return@launch
                }

                // User is logged in, get profile
                val adminProfile = getAdminProfileUseCase()
                if (adminProfile != null) {
                    _sessionState.value = Resource.Success(adminProfile)
                } else {
                    // Profile not found, session might be corrupted
                    _sessionState.value = Resource.Error(
                        AppException.AuthenticationException("Profil admin tidak ditemukan")
                    )
                }
            } catch (e: Exception) {
                _sessionState.value = Resource.Error(
                    AppException.UnknownException("Gagal memeriksa status login")
                )
            }
        }
    }

    /**
     * Perform logout and clear session
     */
    fun logout() {
        logoutUseCase().onEach { result ->
            when (result) {
                is Resource.Success -> {
                    _eventFlow.emit(UiEvent.ShowMessage("Logout berhasil"))
                    _eventFlow.emit(UiEvent.SessionExpired)
                }
                is Resource.Error -> {
                    _eventFlow.emit(UiEvent.ShowMessage("Gagal logout: ${result.message}"))
                    // Even if logout API fails, clear local session
                    _eventFlow.emit(UiEvent.SessionExpired)
                }
                is Resource.Loading -> {
                    // Could show loading indicator if needed
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
        checkSessionValidity()
    }

    sealed class UiEvent {
        object SessionExpired : UiEvent()
        data class ShowMessage(val message: String) : UiEvent()
    }
}
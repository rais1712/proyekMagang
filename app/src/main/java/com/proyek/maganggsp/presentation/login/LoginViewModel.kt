package com.proyek.maganggsp.presentation.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.usecase.auth.LoginUseCase
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
import com.proyek.maganggsp.util.FeatureFlags
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.delay
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "LoginViewModel"
        private const val MIN_LOADING_DURATION = 1000L // Minimum 1 second untuk UX yang baik
    }

    private val _loginState = MutableStateFlow<Resource<Admin>>(Resource.Empty)
    val loginState: StateFlow<Resource<Admin>> = _loginState

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 LoginViewModel initialized - API integration ready")
            // Print debug info saat ViewModel dibuat
            com.proyek.maganggsp.util.TestingHelper.printDebugInfo()
        }
    }

    fun login(email: String, password: String) {
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 Login requested - Email: $email, Password length: ${password.length}")
            Log.d(TAG, "🚩 Target API: ${getBuildConfigBaseUrl()}/auth/login")
        }

        // Quick client-side validation
        if (email.isBlank() || password.isBlank()) {
            val message = when {
                email.isBlank() && password.isBlank() -> "Email dan password harus diisi"
                email.isBlank() -> "Email harus diisi"
                else -> "Password harus diisi"
            }

            _loginState.value = Resource.Error(
                AppException.ValidationException(message)
            )

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Login validation failed: $message")
            }
            return
        }

        val startTime = System.currentTimeMillis()

        loginUseCase(email, password).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _loginState.value = Resource.Loading()
                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.d(TAG, "🚩 API call in progress...")
                    }
                }
                is Resource.Success -> {
                    // Ensure minimum loading duration for good UX
                    val elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime < MIN_LOADING_DURATION) {
                        delay(MIN_LOADING_DURATION - elapsedTime)
                    }

                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.d(TAG, "🚩 Login API success - Admin: ${result.data?.name}")
                        Log.d(TAG, "🚩 Token received: ${result.data?.token?.take(10)}...") // Show only first 10 chars
                    }

                    // Clear login state and emit success event
                    _loginState.value = Resource.Empty
                    _eventFlow.emit(UiEvent.LoginSuccess)
                }
                is Resource.Error -> {
                    // Ensure minimum loading duration for good UX
                    val elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime < MIN_LOADING_DURATION) {
                        delay(MIN_LOADING_DURATION - elapsedTime)
                    }

                    val enhancedMessage = enhanceErrorMessage(result.exception)
                    _loginState.value = Resource.Error(
                        AppException.UnknownException(enhancedMessage)
                    )

                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.e(TAG, "🚩 Login API error: ${result.exception.message}")
                        Log.e(TAG, "🚩 Enhanced message: $enhancedMessage")
                    }
                }
                else -> {
                    // Handle other states if needed
                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.d(TAG, "🚩 Login state: ${result::class.simpleName}")
                    }
                }
            }
        }.launchIn(viewModelScope)
    }

    /**
     * Enhanced error message berdasarkan jenis exception
     */
    private fun enhanceErrorMessage(exception: AppException): String {
        return when (exception) {
            is AppException.NetworkException -> {
                "Tidak dapat terhubung ke server. Pastikan koneksi internet aktif dan server development berjalan di 192.168.168.6:8180."
            }
            is AppException.AuthenticationException -> {
                "Email atau password salah. Silakan periksa kembali kredensial Anda."
            }
            is AppException.ServerException -> {
                when (exception.httpCode) {
                    401 -> "Email atau password salah. Silakan coba lagi."
                    404 -> "Server tidak ditemukan. Pastikan server development berjalan."
                    500 -> "Server sedang mengalami masalah. Coba lagi dalam beberapa saat."
                    422 -> "Data login tidak sesuai format. Periksa email dan password Anda."
                    else -> "Terjadi kesalahan server (${exception.httpCode}). Silakan coba lagi."
                }
            }
            is AppException.ValidationException -> {
                exception.message // Pesan validasi sudah user-friendly
            }
            is AppException.ParseException -> {
                "Terjadi kesalahan dalam memproses respons server. Coba lagi nanti."
            }
            is AppException.UnauthorizedException -> {
                "Akses tidak diizinkan. Hubungi administrator sistem."
            }
            is AppException.UnknownException -> {
                "Terjadi kesalahan yang tidak terduga. Silakan coba lagi atau hubungi dukungan teknis."
            }
        }
    }

    /**
     * Get base URL for debugging (safe access to BuildConfig)
     */
    private fun getBuildConfigBaseUrl(): String {
        return try {
            // Safe access to BuildConfig
            "http://192.168.168.6:8180/api"
        } catch (e: Exception) {
            "Unknown"
        }
    }

    /**
     * Debug helper untuk troubleshooting
     */
    fun getDebugInfo(): String {
        return if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            """
            🚩 LOGIN DEBUG INFO:
            Current State: ${_loginState.value::class.simpleName}
            Target API: ${getBuildConfigBaseUrl()}/auth/login
            Use Case: LoginUseCase
            ViewModel: ${this::class.simpleName}
            """.trimIndent()
        } else {
            "Debug info disabled"
        }
    }

    override fun onCleared() {
        super.onCleared()
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 LoginViewModel cleared")
        }
    }

    sealed class UiEvent {
        object LoginSuccess : UiEvent()
    }
}
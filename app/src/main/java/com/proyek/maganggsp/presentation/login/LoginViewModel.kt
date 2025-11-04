// File: app/src/main/java/com/proyek/maganggsp/presentation/login/LoginViewModel.kt - SIMPLIFIED
package com.proyek.maganggsp.presentation.login

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.usecase.auth.LoginUseCase
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.exceptions.AppException
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
        private const val MIN_LOADING_DURATION = 1000L // Minimum loading for good UX
    }

    private val _loginState = MutableStateFlow<Resource<Admin>>(Resource.Empty())
    val loginState: StateFlow<Resource<Admin>> = _loginState

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    init {
        Log.d(TAG, "🔄 SIMPLIFIED LoginViewModel initialized - FeatureFlags removed")
    }

    fun login(email: String, password: String) {
        Log.d(TAG, "🚀 Login requested - Email: $email, Password length: ${password.length}")

        // Quick client-side validation
        if (email.isBlank() || password.isBlank()) {
            val message = when {
                email.isBlank() && password.isBlank() -> "Email and password are required"
                email.isBlank() -> "Email is required"
                else -> "Password is required"
            }

            _loginState.value = Resource.Error(
                AppException.ValidationException(message)
            )
            Log.w(TAG, "⚠️ Login validation failed: $message")
            return
        }

        val startTime = System.currentTimeMillis()

        loginUseCase(email, password).onEach { result ->
            when (result) {
                is Resource.Loading -> {
                    _loginState.value = Resource.Loading()
                    Log.d(TAG, "⏳ API call in progress...")
                }
                is Resource.Success -> {
                    // Ensure minimum loading duration for good UX
                    val elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime < MIN_LOADING_DURATION) {
                        delay(MIN_LOADING_DURATION - elapsedTime)
                    }

                    Log.d(TAG, "✅ Login API success - Admin: ${result.data?.name}")

                    // Clear login state and emit success event
                    _loginState.value = Resource.Empty()
                    _eventFlow.emit(UiEvent.LoginSuccess)
                }
                is Resource.Error -> {
                    // Ensure minimum loading duration for good UX
                    val elapsedTime = System.currentTimeMillis() - startTime
                    if (elapsedTime < MIN_LOADING_DURATION) {
                        delay(MIN_LOADING_DURATION - elapsedTime)
                    }

                    val enhancedMessage = enhanceErrorMessage(result.message)
                    _loginState.value = Resource.Error(
                        AppException.UnknownException(enhancedMessage)
                    )

                    Log.e(TAG, "❌ Login API error: ${result.message.message}")
                }
                else -> {
                    Log.d(TAG, "📋 Login state: ${result::class.simpleName}")
                }
            }
        }.launchIn(viewModelScope)
    }

    private fun enhanceErrorMessage(exception: AppException): String {
        return when (exception) {
            is AppException.NetworkException -> {
                "Cannot connect to server. Check your internet connection and ensure development server is running at 192.168.168.6:8180."
            }
            is AppException.AuthenticationException -> {
                "Incorrect email or password. Please check your credentials."
            }
            is AppException.ServerException -> {
                when (exception.httpCode) {
                    401 -> "Incorrect email or password. Please try again."
                    404 -> "Server not found. Make sure development server is running."
                    500 -> "Server is experiencing issues. Try again in a moment."
                    422 -> "Login data format is incorrect. Check your email and password."
                    else -> "Server error (${exception.httpCode}). Please try again."
                }
            }
            is AppException.ValidationException -> {
                exception.message // Validation messages are already user-friendly
            }
            is AppException.ParseException -> {
                "Error processing server response. Try again later."
            }
            is AppException.UnauthorizedException -> {
                "Access not authorized. Contact system administrator."
            }
            is AppException.UnknownException -> {
                "An unexpected error occurred. Please try again or contact technical support."
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 LoginViewModel cleared")
    }

    sealed class UiEvent {
        object LoginSuccess : UiEvent()
    }
}
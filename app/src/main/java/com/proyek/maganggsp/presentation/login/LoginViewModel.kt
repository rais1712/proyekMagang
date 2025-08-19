package com.proyek.maganggsp.presentation.login

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
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    // FIXED: Gunakan Resource.Empty sebagai object
    private val _loginState = MutableStateFlow<Resource<Admin>>(Resource.Empty)
    val loginState: StateFlow<Resource<Admin>> = _loginState

    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            // FIXED: Gunakan Resource.Error dengan AppException
            _loginState.value = Resource.Error(
                AppException.ValidationException("Email dan password tidak boleh kosong")
            )
            return
        }

        loginUseCase(email, password).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    // FIXED: Resource.Empty sebagai object
                    _loginState.value = Resource.Empty
                    _eventFlow.emit(UiEvent.LoginSuccess)
                }
                else -> {
                    _loginState.value = result
                }
            }
        }.launchIn(viewModelScope)
    }

    sealed class UiEvent {
        object LoginSuccess : UiEvent()
    }
}
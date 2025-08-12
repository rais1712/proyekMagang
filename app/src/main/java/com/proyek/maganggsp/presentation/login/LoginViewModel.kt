package com.proyek.maganggsp.presentation.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.model.Admin
import com.proyek.maganggsp.domain.usecase.auth.LoginUseCase
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import javax.inject.Inject
import com.proyek.maganggsp.util.Resource.Empty

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase
) : ViewModel() {

    // StateFlow untuk state UI (Loading, Error, dll)
    private val _loginState = MutableStateFlow<Resource<Admin>>(Empty())
    val loginState: StateFlow<Resource<Admin>> = _loginState

    // SharedFlow untuk event sekali jalan (seperti navigasi)
    private val _eventFlow = MutableSharedFlow<UiEvent>()
    val eventFlow = _eventFlow.asSharedFlow()

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _loginState.value = Resource.Error("Email dan password tidak boleh kosong.")
            return
        }

        loginUseCase(email, password).onEach { result ->
            when (result) {
                is Resource.Success -> {
                    // Jika sukses, jangan kirim data ke _loginState,
                    // tapi kirim event untuk navigasi
                    _loginState.value = Empty() // Reset state UI
                    _eventFlow.emit(UiEvent.LoginSuccess)
                }
                else -> {
                    // Untuk Loading dan Error, tetap gunakan _loginState
                    _loginState.value = result
                }
            }
        }.launchIn(viewModelScope)
    }

    // Sealed class untuk mendefinisikan event-event yang bisa dikirim
    sealed class UiEvent {
        object LoginSuccess : UiEvent()
    }
}
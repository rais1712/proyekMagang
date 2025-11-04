// File: app/src/main/java/com/proyek/maganggsp/presentation/profile/UpdateProfileViewModel.kt
package com.proyek.maganggsp.presentation.profile

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.proyek.maganggsp.domain.usecase.profile.UpdateProfileUseCase
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class UpdateProfileViewModel @Inject constructor(
    private val updateProfileUseCase: UpdateProfileUseCase
) : ViewModel() {

    companion object {
        private const val TAG = "UpdateProfileViewModel"
    }

    private val _updateState = MutableStateFlow<Resource<Unit>>(Resource.Empty())
    val updateState: StateFlow<Resource<Unit>> = _updateState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<UiEvent>(
        extraBufferCapacity = 1,
        onBufferOverflow = kotlinx.coroutines.channels.BufferOverflow.DROP_OLDEST
    )
    val eventFlow = _eventFlow.asSharedFlow()

    private var currentPpid: String? = null

    init {
        Log.d(TAG, "🔄 UpdateProfileViewModel initialized")
    }

    fun setCurrentPpid(ppid: String) {
        currentPpid = ppid
        Log.d(TAG, "📋 Current ppid set: $ppid")
    }

    fun updateProfile(currentPpid: String, newPpid: String) {
        Log.d(TAG, "🔄 Update profile request: $currentPpid -> $newPpid")

        // Validation
        if (!validateUpdateRequest(currentPpid, newPpid)) {
            return
        }

        updateProfileUseCase(currentPpid, newPpid).onEach { result ->
            _updateState.value = result

            when (result) {
                is Resource.Success -> {
                    Log.d(TAG, "✅ Profile update berhasil")
                    emitUiEvent(UiEvent.UpdateSuccess(newPpid))
                }
                is Resource.Error -> {
                    Log.e(TAG, "❌ Profile update gagal: ${result.message}")
                    emitUiEvent(UiEvent.ShowToast("Gagal update profil: ${result.message}"))
                }
                is Resource.Loading -> {
                    Log.d(TAG, "⏳ Update profil dalam proses...")
                }
                else -> Unit
            }
        }.launchIn(viewModelScope)
    }

    private fun validateUpdateRequest(currentPpid: String, newPpid: String): Boolean {
        when {
            currentPpid.isBlank() -> {
                emitUiEvent(UiEvent.ShowToast("PPID saat ini tidak valid"))
                return false
            }
            newPpid.isBlank() -> {
                emitUiEvent(UiEvent.ShowToast("PPID baru tidak boleh kosong"))
                return false
            }
            newPpid.length < 5 -> {
                emitUiEvent(UiEvent.ShowToast("PPID baru minimal 5 karakter"))
                return false
            }
            currentPpid == newPpid -> {
                emitUiEvent(UiEvent.ShowToast("PPID baru harus berbeda dari yang lama"))
                return false
            }
            else -> {
                Log.d(TAG, "✅ Validation passed untuk update: $currentPpid -> $newPpid")
                return true
            }
        }
    }

    private fun emitUiEvent(event: UiEvent) {
        viewModelScope.launch {
            try {
                _eventFlow.emit(event)
            } catch (e: Exception) {
                Log.e(TAG, "❌ Error emitting UI event", e)
            }
        }
    }

    fun onUpdateConsumed() {
        _updateState.value = Resource.Empty()
        Log.d(TAG, "🧹 Update state consumed")
    }

    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "🧹 UpdateProfileViewModel cleared")
    }

    sealed class UiEvent {
        data class ShowToast(val message: String) : UiEvent()
        data class UpdateSuccess(val newPpid: String) : UiEvent()
    }
}
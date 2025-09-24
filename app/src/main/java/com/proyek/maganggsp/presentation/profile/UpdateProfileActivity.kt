// File: app/src/main/java/com/proyek/maganggsp/presentation/profile/UpdateProfileActivity.kt
package com.proyek.maganggsp.presentation.profile

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ActivityUpdateProfileBinding
import com.proyek.maganggsp.util.NavigationConstants
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class UpdateProfileActivity : AppCompatActivity() {

    private lateinit var binding: ActivityUpdateProfileBinding
    private val viewModel: UpdateProfileViewModel by viewModels()

    companion object {
        private const val TAG = "UpdateProfileActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityUpdateProfileBinding.inflate(layoutInflater)
        setContentView(binding.root)

        Log.d(TAG, "🔄 UpdateProfileActivity created - Separate screen untuk update profile")

        setupToolbar()
        setupUI()
        setupValidation()
        setupListeners()
        observeViewModel()

        // Get current ppid dari intent
        val currentPpid = intent.getStringExtra(NavigationConstants.ARG_PPID)
            ?: intent.getStringExtra("currentPpid")
            ?: ""

        if (currentPpid.isNotBlank()) {
            viewModel.setCurrentPpid(currentPpid)
            binding.etCurrentPpid.setText(currentPpid)
        } else {
            AppUtils.showError(this, "PPID tidak valid")
            finish()
        }
    }

    private fun setupToolbar() {
        supportActionBar?.title = "Update Profile"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupUI() {
        // Setup initial UI state
        binding.etCurrentPpid.isEnabled = false // Read-only untuk current ppid
        binding.btnSave.isEnabled = false

        // Focus pada input ppid baru
        binding.etNewPpid.requestFocus()
    }

    private fun setupValidation() {
        // Real-time validation untuk new ppid
        binding.etNewPpid.addTextChangedListener { editable ->
            val newPpid = editable.toString().trim()
            val currentPpid = binding.etCurrentPpid.text.toString().trim()

            val isValid = validateNewPpid(newPpid, currentPpid)
            binding.btnSave.isEnabled = isValid

            // Clear error jika valid
            if (isValid) {
                binding.tilNewPpid.error = null
            }
        }
    }

    private fun validateNewPpid(newPpid: String, currentPpid: String): Boolean {
        return when {
            newPpid.isBlank() -> {
                binding.tilNewPpid.error = "PPID baru wajib diisi"
                false
            }
            newPpid.length < 5 -> {
                binding.tilNewPpid.error = "PPID minimal 5 karakter"
                false
            }
            newPpid == currentPpid -> {
                binding.tilNewPpid.error = "PPID baru harus berbeda dari yang lama"
                false
            }
            !AppUtils.isValidPpid(newPpid) -> {
                binding.tilNewPpid.error = "Format PPID tidak valid"
                false
            }
            else -> {
                binding.tilNewPpid.error = null
                true
            }
        }
    }

    private fun setupListeners() {
        // Save button
        binding.btnSave.setOnClickListener {
            val currentPpid = binding.etCurrentPpid.text.toString().trim()
            val newPpid = binding.etNewPpid.text.toString().trim()

            if (validateNewPpid(newPpid, currentPpid)) {
                showConfirmationDialog(currentPpid, newPpid)
            }
        }

        // Cancel button
        binding.btnCancel.setOnClickListener {
            finish()
        }
    }

    private fun showConfirmationDialog(currentPpid: String, newPpid: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Konfirmasi Update")
            .setMessage("Yakin ingin mengubah PPID dari:\n\n$currentPpid\n\nke:\n\n$newPpid")
            .setPositiveButton("Ya") { _, _ ->
                Log.d(TAG, "🔄 User konfirmasi update profile: $currentPpid -> $newPpid")
                viewModel.updateProfile(currentPpid, newPpid)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun observeViewModel() {
        // Observe update state
        lifecycleScope.launch {
            viewModel.updateState.collectLatest { resource ->
                handleUpdateState(resource)
            }
        }

        // Observe events
        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest { event ->
                handleViewModelEvents(event)
            }
        }
    }

    private fun handleUpdateState(resource: Resource<Unit>) {
        val isLoading = resource is Resource.Loading

        // Update UI loading state
        binding.btnSave.isEnabled = !isLoading && validateNewPpid(
            binding.etNewPpid.text.toString().trim(),
            binding.etCurrentPpid.text.toString().trim()
        )
        binding.btnCancel.isEnabled = !isLoading
        binding.etNewPpid.isEnabled = !isLoading

        // Update button text
        binding.btnSave.text = if (isLoading) "Menyimpan..." else "Simpan"

        when (resource) {
            is Resource.Success -> {
                Log.d(TAG, "✅ Update profile berhasil")
                AppUtils.showSuccess(this, "Profil berhasil diupdate")

                // Set result dan finish
                setResult(RESULT_OK)
                finish()
            }
            is Resource.Error -> {
                Log.e(TAG, "❌ Update profile gagal: ${resource.message}")
                AppUtils.showError(this, resource.exception)
            }
            is Resource.Loading -> {
                Log.d(TAG, "⏳ Update profile dalam proses...")
            }
            else -> Unit
        }
    }

    private fun handleViewModelEvents(event: UpdateProfileViewModel.UiEvent) {
        when (event) {
            is UpdateProfileViewModel.UiEvent.ShowToast -> {
                AppUtils.showError(this, event.message)
            }
            is UpdateProfileViewModel.UiEvent.UpdateSuccess -> {
                AppUtils.showSuccess(this, "Profil berhasil diupdate menjadi: ${event.newPpid}")
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressedDispatcher.onBackPressed()
        return true
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🧹 UpdateProfileActivity destroyed")
    }
}
package com.proyek.maganggsp.presentation.login

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ActivityLoginBinding
import com.proyek.maganggsp.presentation.main.MainActivity
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()
    private var isPasswordVisible = false

    companion object {
        private const val TAG = "LoginActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 LoginActivity created - API integration ready")
        }

        setupUI()
        setupListeners()
        observeUiState()
        observeUiEvents()
    }

    private fun setupUI() {
        // Pre-fill email untuk testing (hanya di debug mode)
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            binding.etEmail.setText("lalan@gsp.co.id")
            binding.etPassword.setText("123456")
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Debug mode - pre-filled test credentials")
            }
        }

        // Setup password toggle visibility
        updatePasswordToggleIcon()
    }

    private fun setupListeners() {
        // Login button click
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Login attempt - Email: $email, Password length: ${password.length}")
            }

            performLogin(email, password)
        }

        // Password toggle click
        binding.ivTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Input validation on text change
        binding.etEmail.addTextChangedListener {
            clearLoginError()
        }

        binding.etPassword.addTextChangedListener {
            clearLoginError()
        }

        // Enter key handling
        binding.etPassword.setOnEditorActionListener { _, _, _ ->
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            performLogin(email, password)
            true
        }
    }

    private fun performLogin(email: String, password: String) {
        // Clear previous errors
        clearLoginError()

        // Hide keyboard
        hideKeyboard()

        // Start login process
        viewModel.login(email, password)
    }

    private fun togglePasswordVisibility() {
        isPasswordVisible = !isPasswordVisible

        if (isPasswordVisible) {
            binding.etPassword.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
            binding.ivTogglePassword.setImageResource(android.R.drawable.ic_menu_close_clear_cancel)
        } else {
            binding.etPassword.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
            binding.ivTogglePassword.setImageResource(android.R.drawable.ic_menu_view)
        }

        // Move cursor to end
        binding.etPassword.setSelection(binding.etPassword.text.length)
        updatePasswordToggleIcon()
    }

    private fun updatePasswordToggleIcon() {
        val iconRes = if (isPasswordVisible) {
            android.R.drawable.ic_menu_close_clear_cancel
        } else {
            android.R.drawable.ic_menu_view
        }
        binding.ivTogglePassword.setImageResource(iconRes)
    }

    private fun clearLoginError() {
        // Remove error styling if any
        binding.etEmail.error = null
        binding.etPassword.error = null
    }

    private fun hideKeyboard() {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.etPassword.windowToken, 0)
    }

    // Observer untuk state UI (Loading, Error)
    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.loginState.collectLatest { resource ->
                handleLoadingState(resource is Resource.Loading)

                when (resource) {
                    is Resource.Loading -> {
                        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                            Log.d(TAG, "🚩 Login in progress...")
                        }
                    }
                    is Resource.Error -> {
                        handleLoginError(resource.message)
                        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                            Log.e(TAG, "🚩 Login error: ${resource.message}")
                        }
                    }
                    is Resource.Success -> {
                        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                            Log.d(TAG, "🚩 Login success received in state")
                        }
                    }
                    else -> {
                        // Empty or other states
                    }
                }
            }
        }
    }

    // Observer khusus untuk event sekali jalan (Navigasi)
    private fun observeUiEvents() {
        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is LoginViewModel.UiEvent.LoginSuccess -> {
                        handleLoginSuccess()
                    }
                }
            }
        }
    }

    private fun handleLoadingState(isLoading: Boolean) {
        binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        binding.btnLogin.isEnabled = !isLoading
        binding.etEmail.isEnabled = !isLoading
        binding.etPassword.isEnabled = !isLoading

        if (isLoading) {
            binding.btnLogin.text = "Masuk..."
        } else {
            binding.btnLogin.text = "Log In"
        }
    }

    private fun handleLoginError(message: String) {
        // Show user-friendly error message
        val userMessage = getUserFriendlyErrorMessage(message)

        Toast.makeText(this, userMessage, Toast.LENGTH_LONG).show()

        // Focus on appropriate field based on error type
        when {
            message.contains("email", ignoreCase = true) -> {
                binding.etEmail.requestFocus()
                binding.etEmail.error = "Periksa email Anda"
            }
            message.contains("password", ignoreCase = true) -> {
                binding.etPassword.requestFocus()
                binding.etPassword.error = "Periksa password Anda"
            }
            message.contains("koneksi", ignoreCase = true) ||
                    message.contains("network", ignoreCase = true) -> {
                // Network error - no specific field focus
                binding.etEmail.requestFocus()
            }
            else -> {
                binding.etEmail.requestFocus()
            }
        }
    }

    private fun getUserFriendlyErrorMessage(originalMessage: String): String {
        return when {
            originalMessage.contains("401") || originalMessage.contains("salah") ->
                "Email atau password salah. Silakan coba lagi."
            originalMessage.contains("network") || originalMessage.contains("koneksi") ->
                "Masalah koneksi internet. Periksa jaringan Anda."
            originalMessage.contains("server") || originalMessage.contains("500") ->
                "Server sedang bermasalah. Coba lagi nanti."
            originalMessage.contains("email") && originalMessage.contains("kosong") ->
                "Email harus diisi."
            originalMessage.contains("password") && originalMessage.contains("kosong") ->
                "Password harus diisi."
            originalMessage.contains("email") && originalMessage.contains("valid") ->
                "Format email tidak valid."
            originalMessage.length > 100 ->
                "Terjadi kesalahan saat login. Silakan coba lagi."
            else -> originalMessage
        }
    }

    private fun handleLoginSuccess() {
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 Login successful - navigating to MainActivity")
        }

        // Show success message
        Toast.makeText(this, "Login berhasil! Selamat datang.", Toast.LENGTH_SHORT).show()

        // Navigate to MainActivity
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()

        // Add transition animation
        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    override fun onResume() {
        super.onResume()
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 LoginActivity resumed")
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 LoginActivity destroyed")
        }
    }
}
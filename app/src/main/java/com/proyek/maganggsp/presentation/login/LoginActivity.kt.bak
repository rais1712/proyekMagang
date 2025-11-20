// File: app/src/main/java/com/proyek/maganggsp/presentation/login/LoginActivity.kt - SIMPLIFIED
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

        Log.d(TAG, "🔄 SIMPLIFIED LoginActivity created - FeatureFlags removed")

        setupUI()
        setupListeners()
        observeUiState()
        observeUiEvents()
    }

    private fun setupUI() {
        // Pre-fill credentials for testing in debug builds
        if (com.proyek.maganggsp.BuildConfig.DEBUG) {
            binding.etEmail.setText("lalan@gsp.co.id")
            binding.etPassword.setText("123456")
            Log.d(TAG, "🔧 Debug mode - pre-filled test credentials")
        }

        updatePasswordToggleIcon()
    }

    private fun setupListeners() {
        // Login button click
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            Log.d(TAG, "🚀 Login attempt - Email: $email, Password length: ${password.length}")
            performLogin(email, password)
        }

        // Password toggle click
        binding.ivTogglePassword.setOnClickListener {
            togglePasswordVisibility()
        }

        // Input validation on text change
        binding.etEmail.addTextChangedListener { clearLoginError() }
        binding.etPassword.addTextChangedListener { clearLoginError() }

        // Enter key handling
        binding.etPassword.setOnEditorActionListener { _, _, _ ->
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            performLogin(email, password)
            true
        }
    }

    private fun performLogin(email: String, password: String) {
        clearLoginError()
        hideKeyboard()
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
        binding.etEmail.error = null
        binding.etPassword.error = null
    }

    private fun hideKeyboard() {
        val imm = getSystemService(android.content.Context.INPUT_METHOD_SERVICE) as android.view.inputmethod.InputMethodManager
        imm.hideSoftInputFromWindow(binding.etPassword.windowToken, 0)
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.loginState.collectLatest { resource ->
                handleLoadingState(resource is Resource.Loading)

                when (resource) {
                    is Resource.Loading -> {
                        Log.d(TAG, "⏳ Login in progress...")
                    }
                    is Resource.Error -> {
                        handleLoginError(resource.message)
                        Log.e(TAG, "❌ Login error: ${resource.message}")
                    }
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Login success received in state")
                    }
                    else -> {
                        // Empty or other states
                    }
                }
            }
        }
    }

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

        binding.btnLogin.text = if (isLoading) "Logging in..." else "Log In"
    }

    private fun handleLoginError(message: String) {
        val userMessage = getUserFriendlyErrorMessage(message)
        Toast.makeText(this, userMessage, Toast.LENGTH_LONG).show()

        // Focus on appropriate field based on error type
        when {
            message.contains("email", ignoreCase = true) -> {
                binding.etEmail.requestFocus()
                binding.etEmail.error = "Check your email"
            }
            message.contains("password", ignoreCase = true) -> {
                binding.etPassword.requestFocus()
                binding.etPassword.error = "Check your password"
            }
            message.contains("connection", ignoreCase = true) ||
                    message.contains("network", ignoreCase = true) -> {
                binding.etEmail.requestFocus()
            }
            else -> {
                binding.etEmail.requestFocus()
            }
        }
    }

    private fun getUserFriendlyErrorMessage(originalMessage: String): String {
        return when {
            originalMessage.contains("401") || originalMessage.contains("wrong") ->
                "Incorrect email or password. Please try again."
            originalMessage.contains("network") || originalMessage.contains("connection") ->
                "Network connection issue. Please check your internet."
            originalMessage.contains("server") || originalMessage.contains("500") ->
                "Server is having issues. Try again later."
            originalMessage.contains("email") && originalMessage.contains("empty") ->
                "Email is required."
            originalMessage.contains("password") && originalMessage.contains("empty") ->
                "Password is required."
            originalMessage.contains("email") && originalMessage.contains("valid") ->
                "Invalid email format."
            originalMessage.length > 100 ->
                "Login error occurred. Please try again."
            else -> originalMessage
        }
    }

    private fun handleLoginSuccess() {
        Log.d(TAG, "✅ Login successful - navigating to MainActivity")

        Toast.makeText(this, "Login successful! Welcome.", Toast.LENGTH_SHORT).show()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()

        overridePendingTransition(android.R.anim.slide_in_left, android.R.anim.slide_out_right)
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "🔄 LoginActivity resumed")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🧹 LoginActivity destroyed")
    }
}
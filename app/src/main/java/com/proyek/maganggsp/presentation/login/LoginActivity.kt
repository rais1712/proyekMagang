package com.proyek.maganggsp.presentation.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.proyek.maganggsp.databinding.ActivityLoginBinding
import com.proyek.maganggsp.presentation.MainActivity
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private val viewModel: LoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupListeners()
        observeUiState()
        observeUiEvents() // <<< Tambahkan observer baru
    }

    private fun setupListeners() {
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            viewModel.login(email, password)
        }
    }

    // Observer untuk state UI (Loading, Error)
    private fun observeUiState() {
        lifecycleScope.launch {
            viewModel.loginState.collectLatest { resource ->
                binding.progressBar.visibility = if (resource is Resource.Loading) View.VISIBLE else View.GONE
                binding.btnLogin.isEnabled = resource !is Resource.Loading

                if (resource is Resource.Error) {
                    Toast.makeText(this@LoginActivity, resource.message, Toast.LENGTH_LONG).show()
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
                        Toast.makeText(this@LoginActivity, "Login Berhasil!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this@LoginActivity, MainActivity::class.java))
                        finish()
                    }
                }
            }
        }
    }
}
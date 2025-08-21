// MODERNIZED: Updated MainActivity with latest Android practices
// File: app/src/main/java/com/proyek/maganggsp/presentation/MainActivity.kt

package com.proyek.maganggsp.presentation

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ActivityMainBinding
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()

    // MODERNIZED: Use OnBackPressedCallback instead of deprecated onBackPressed
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleBackNavigation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupBackPressedHandler()
        observeViewModel()

        // Check session validity on app start
        viewModel.checkSessionValidity()
    }

    private fun setupNavigation() {
        // Setup NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // Setup BottomNavigation with NavController
        binding.bottomNavigation.setupWithNavController(navController)

        // ENHANCED: Handle navigation item reselection
        binding.bottomNavigation.setOnItemReselectedListener { item ->
            when (item.itemId) {
                R.id.homeFragment -> {
                    // Scroll to top or refresh if already on home
                    // This can be enhanced with fragment communication
                }
                R.id.historyFragment -> {
                    // Refresh history if already on history fragment
                }
                R.id.monitorFragment -> {
                    // Refresh monitor data if already on monitor fragment
                }
            }
        }

        // ENHANCED: Navigation destination change listener
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // Show/hide bottom navigation based on destination
            when (destination.id) {
                R.id.homeFragment,
                R.id.historyFragment,
                R.id.monitorFragment -> {
                    binding.bottomNavigation.isVisible = true
                }
                else -> {
                    binding.bottomNavigation.isVisible = false
                }
            }

            // Update back button behavior based on destination
            onBackPressedCallback.isEnabled = true
        }
    }

    private fun setupBackPressedHandler() {
        // MODERNIZED: Use OnBackPressedDispatcher
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun handleBackNavigation() {
        when (navController.currentDestination?.id) {
            R.id.homeFragment -> {
                // If on HomeFragment, exit app with confirmation
                showExitConfirmation()
            }
            R.id.historyFragment,
            R.id.monitorFragment -> {
                // From other main fragments, go to HomeFragment
                navController.navigate(R.id.homeFragment)
            }
            else -> {
                // For other destinations, use default back behavior
                if (!navController.popBackStack()) {
                    // If no back stack, go to home
                    navController.navigate(R.id.homeFragment)
                }
            }
        }
    }

    private fun showExitConfirmation() {
        // ENHANCED: Exit confirmation dialog
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Keluar Aplikasi")
            .setMessage("Apakah Anda yakin ingin keluar dari GesPay Admin?")
            .setPositiveButton("Ya") { _, _ ->
                finish()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.sessionState.collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        // Session invalid, redirect to login
                        handleSessionExpired()
                    }
                    is Resource.Success -> {
                        // Session valid, continue normal operation
                    }
                    else -> {
                        // Handle loading or empty states if needed
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is MainViewModel.UiEvent.SessionExpired -> {
                        handleSessionExpired()
                    }
                    is MainViewModel.UiEvent.ShowMessage -> {
                        // Show toast or snackbar
                        androidx.core.content.ContextCompat.getMainExecutor(this@MainActivity).execute {
                            android.widget.Toast.makeText(
                                this@MainActivity,
                                event.message,
                                android.widget.Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    private fun handleSessionExpired() {
        // ENHANCED: Session expiry handling
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Sesi Berakhir")
            .setMessage("Sesi login Anda telah berakhir. Silakan login kembali.")
            .setPositiveButton("Login") { _, _ ->
                // Navigate to login screen
                val intent = android.content.Intent(this, com.proyek.maganggsp.presentation.login.LoginActivity::class.java)
                intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            }
            .setCancelable(false)
            .show()
    }

    // ENHANCED: Handle configuration changes gracefully
    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        // Save any necessary state
    }

    override fun onRestoreInstanceState(savedInstanceState: Bundle) {
        super.onRestoreInstanceState(savedInstanceState)
        // Restore any necessary state
    }
}
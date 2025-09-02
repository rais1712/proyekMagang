// File: app/src/main/java/com/proyek/maganggsp/presentation/main/MainActivity.kt
package com.proyek.maganggsp.presentation.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ActivityMainBinding
import com.proyek.maganggsp.presentation.login.LoginActivity
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController: NavController
    private val viewModel: MainViewModel by viewModels()

    companion object {
        private const val TAG = "MainActivity"
    }

    // Modern back press handling
    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleBackNavigation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🚩 FEATURE FLAGS: Log current configuration
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.i(TAG, "🚩 Starting MainActivity with Feature Flags:")
            Log.i(TAG, FeatureFlags.getFeatureSummary())
        }

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupNavigation()
        setupBackPressedHandler()
        setupLogoutFeature()
        observeViewModel()

        // Check session validity on app start
        viewModel.checkSessionValidity()
    }

    private fun setupNavigation() {
        // Setup NavController
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        // 🚩 FEATURE FLAGS: Conditional Bottom Navigation Setup
        if (FeatureFlags.ENABLE_BOTTOM_NAVIGATION) {
            configureBottomNavigationForFeatureFlags()

            // Setup with navController AFTER configuration
            binding.bottomNavigation.setupWithNavController(navController)

            // Handle navigation item reselection
            binding.bottomNavigation.setOnItemReselectedListener { item ->
                when (item.itemId) {
                    R.id.homeFragment -> {
                        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                            Log.d(TAG, "🚩 Home fragment reselected")
                        }
                    }
                    R.id.historyFragment -> {
                        if (FeatureFlags.ENABLE_HISTORY_FRAGMENT) {
                            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                                Log.d(TAG, "🚩 History fragment reselected")
                            }
                        }
                    }
                    R.id.monitorFragment -> {
                        if (FeatureFlags.ENABLE_MONITOR_FRAGMENT) {
                            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                                Log.d(TAG, "🚩 Monitor fragment reselected")
                            }
                        }
                    }
                }
            }

            // 🚩 SAFE NAVIGATION: Handle item selection with feature flag checks
            binding.bottomNavigation.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.homeFragment -> {
                        // Home is always available
                        navController.navigate(R.id.homeFragment)
                        true
                    }
                    R.id.historyFragment -> {
                        if (FeatureFlags.ENABLE_HISTORY_FRAGMENT) {
                            try {
                                navController.navigate(R.id.historyFragment)
                                true
                            } catch (e: Exception) {
                                showFeatureTemporarilyDisabled("Riwayat")
                                false
                            }
                        } else {
                            showFeatureTemporarilyDisabled("Riwayat")
                            false
                        }
                    }
                    R.id.monitorFragment -> {
                        if (FeatureFlags.ENABLE_MONITOR_FRAGMENT) {
                            try {
                                navController.navigate(R.id.monitorFragment)
                                true
                            } catch (e: Exception) {
                                showFeatureTemporarilyDisabled("Monitor")
                                false
                            }
                        } else {
                            showFeatureTemporarilyDisabled("Monitor")
                            false
                        }
                    }
                    else -> false
                }
            }

        } else {
            // Hide bottom navigation if disabled
            binding.bottomNavigation.isVisible = false
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Bottom navigation disabled by feature flag")
            }
        }

        // Navigation destination listener
        navController.addOnDestinationChangedListener { _, destination, _ ->
            // 🚩 FEATURE FLAGS: Conditional navigation visibility
            when (destination.id) {
                R.id.homeFragment -> {
                    binding.bottomNavigation.isVisible = FeatureFlags.ENABLE_BOTTOM_NAVIGATION
                    updateActionBarForHome()
                }
                R.id.historyFragment -> {
                    binding.bottomNavigation.isVisible = FeatureFlags.ENABLE_BOTTOM_NAVIGATION && FeatureFlags.ENABLE_HISTORY_FRAGMENT
                    updateActionBarForHistory()
                }
                R.id.monitorFragment -> {
                    binding.bottomNavigation.isVisible = FeatureFlags.ENABLE_BOTTOM_NAVIGATION && FeatureFlags.ENABLE_MONITOR_FRAGMENT
                    updateActionBarForMonitor()
                }
                else -> {
                    binding.bottomNavigation.isVisible = false
                }
            }

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Navigated to: ${destination.label}")
            }
        }
    }

    private fun configureBottomNavigationForFeatureFlags() {
        val menu = binding.bottomNavigation.menu

        // 🚩 FEATURE FLAGS: Hide disabled fragments
        if (!FeatureFlags.ENABLE_HISTORY_FRAGMENT) {
            menu.findItem(R.id.historyFragment)?.isVisible = false
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 History fragment hidden by feature flag")
            }
        }

        if (!FeatureFlags.ENABLE_MONITOR_FRAGMENT) {
            menu.findItem(R.id.monitorFragment)?.isVisible = false
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Monitor fragment hidden by feature flag")
            }
        }
    }

    private fun showFeatureTemporarilyDisabled(featureName: String) {
        android.widget.Toast.makeText(
            this,
            "Fitur $featureName sedang dikembangkan",
            android.widget.Toast.LENGTH_SHORT
        ).show()

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.w(TAG, "🚩 Feature temporarily disabled message shown: $featureName")
        }
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    private fun setupLogoutFeature() {
        // 🚩 FEATURE FLAGS: Only setup logout if enabled
        if (FeatureFlags.ENABLE_LOGOUT) {
            // We'll add logout menu in onCreateOptionsMenu
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Logout feature enabled")
            }
        } else {
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Logout feature disabled")
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        // 🚩 FEATURE FLAGS: Add logout menu only if enabled
        if (FeatureFlags.ENABLE_LOGOUT) {
            menuInflater.inflate(R.menu.main_menu, menu)

            // 🚩 FEATURE FLAGS: Show debug info only in debug mode
            menu?.findItem(R.id.action_debug_info)?.isVisible = FeatureFlags.ENABLE_DEBUG_LOGGING

            return true
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                // 🚩 FEATURE FLAGS: Handle logout only if enabled
                if (FeatureFlags.ENABLE_LOGOUT) {
                    showLogoutConfirmation()
                } else {
                    showFeatureTemporarilyDisabled("Logout")
                }
                true
            }
            R.id.action_debug_info -> {
                // 🚩 FEATURE FLAGS: Debug info only in debug mode
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    showDebugInfo()
                } else {
                    showFeatureTemporarilyDisabled("Debug Info")
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleBackNavigation() {
        when (navController.currentDestination?.id) {
            R.id.homeFragment -> {
                // From home, show exit confirmation
                showExitConfirmation()
            }
            R.id.historyFragment -> {
                // 🚩 FEATURE FLAGS: Only navigate if fragments are enabled
                if (FeatureFlags.ENABLE_HISTORY_FRAGMENT) {
                    try {
                        navController.navigate(R.id.homeFragment)
                    } catch (e: Exception) {
                        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                            Log.e(TAG, "🚩 Navigation error from history to home", e)
                        }
                        showExitConfirmation()
                    }
                } else {
                    showExitConfirmation()
                }
            }
            R.id.monitorFragment -> {
                // 🚩 FEATURE FLAGS: Only navigate if fragments are enabled
                if (FeatureFlags.ENABLE_MONITOR_FRAGMENT) {
                    try {
                        navController.navigate(R.id.homeFragment)
                    } catch (e: Exception) {
                        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                            Log.e(TAG, "🚩 Navigation error from monitor to home", e)
                        }
                        showExitConfirmation()
                    }
                } else {
                    showExitConfirmation()
                }
            }
            else -> {
                // Try to pop back stack, fallback to home
                if (!navController.popBackStack()) {
                    try {
                        navController.navigate(R.id.homeFragment)
                    } catch (e: Exception) {
                        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                            Log.e(TAG, "🚩 Navigation error to home fallback", e)
                        }
                        showExitConfirmation()
                    }
                }
            }
        }
    }

    private fun updateActionBarForHome() {
        supportActionBar?.title = "GesPay Admin"
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
    }

    private fun updateActionBarForHistory() {
        if (FeatureFlags.ENABLE_HISTORY_FRAGMENT) {
            supportActionBar?.title = "Riwayat"
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun updateActionBarForMonitor() {
        if (FeatureFlags.ENABLE_MONITOR_FRAGMENT) {
            supportActionBar?.title = "Monitor"
            supportActionBar?.setDisplayHomeAsUpEnabled(false)
        }
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Keluar Aplikasi")
            .setMessage("Apakah Anda yakin ingin keluar dari GesPay Admin?")
            .setPositiveButton("Ya") { _, _ ->
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.d(TAG, "🚩 User confirmed app exit")
                }
                finish()
            }
            .setNegativeButton("Tidak") { _, _ ->
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.d(TAG, "🚩 User cancelled app exit")
                }
            }
            .show()
    }

    private fun showLogoutConfirmation() {
        if (!FeatureFlags.ENABLE_LOGOUT) {
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Logout attempt blocked by feature flag")
            }
            showFeatureTemporarilyDisabled("Logout")
            return
        }

        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Apakah Anda yakin ingin logout?")
            .setPositiveButton("Ya") { _, _ ->
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.d(TAG, "🚩 User confirmed logout")
                }
                viewModel.logout()
            }
            .setNegativeButton("Tidak") { _, _ ->
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.d(TAG, "🚩 User cancelled logout")
                }
            }
            .show()
    }

    private fun showDebugInfo() {
        if (!FeatureFlags.ENABLE_DEBUG_LOGGING) return

        val debugInfo = """
            🚩 FEATURE FLAGS STATUS:
            ${FeatureFlags.getFeatureSummary()}
            
            📱 SESSION INFO:
            ${viewModel.getSessionDebugInfo()}
            
            🧭 NAVIGATION INFO:
            Current: ${navController.currentDestination?.label}
            Enabled Items: ${FeatureFlags.getEnabledBottomNavItems().joinToString(", ")}
        """.trimIndent()

        AlertDialog.Builder(this)
            .setTitle("Debug Info")
            .setMessage(debugInfo)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.sessionState.collectLatest { resource ->
                when (resource) {
                    is Resource.Error -> {
                        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                            Log.e(TAG, "🚩 Session error: ${resource.message}")
                        }
                        handleSessionExpired()
                    }
                    is Resource.Success -> {
                        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                            Log.d(TAG, "🚩 Session valid")
                        }
                    }
                    else -> {
                        // Handle loading or empty states
                    }
                }
            }
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is MainViewModel.UiEvent.SessionExpired -> {
                        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                            Log.w(TAG, "🚩 Session expired event received")
                        }
                        handleSessionExpired()
                    }
                    is MainViewModel.UiEvent.ShowMessage -> {
                        android.widget.Toast.makeText(this@MainActivity, event.message, android.widget.Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun handleSessionExpired() {
        AlertDialog.Builder(this)
            .setTitle("Sesi Berakhir")
            .setMessage("Sesi login Anda telah berakhir. Silakan login kembali.")
            .setPositiveButton("Login") { _, _ ->
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.d(TAG, "🚩 Redirecting to login after session expiry")
                }
                navigateToLogin()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        startActivity(intent)
        finish()
    }
}
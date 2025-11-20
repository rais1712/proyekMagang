// File: app/src/main/java/com/proyek/maganggsp/presentation/main/MainActivity.kt - SIMPLIFIED
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

    private val onBackPressedCallback = object : OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            handleBackNavigation()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "🔄 SIMPLIFIED MainActivity created - FeatureFlags removed")

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

        // Setup BottomNavigation with all items enabled
        binding.bottomNavigation.setupWithNavController(navController)

        // Handle navigation item reselection
        binding.bottomNavigation.setOnItemReselectedListener { item ->
            Log.d(TAG, "📱 Bottom nav item reselected: ${item.title}")
            // No special action on reselection
        }

        // Navigation destination listener
        navController.addOnDestinationChangedListener { _, destination, _ ->
            updateActionBarForDestination(destination.id)
            Log.d(TAG, "📱 Navigated to: ${destination.label}")
        }

        Log.d(TAG, "✅ Navigation setup completed - all destinations enabled")
    }

    private fun updateActionBarForDestination(destinationId: Int) {
        when (destinationId) {
            R.id.homeFragment -> {
                supportActionBar?.title = "GesPay Admin"
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
            R.id.historyFragment -> {
                supportActionBar?.title = "History"
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
            R.id.monitorFragment -> {
                supportActionBar?.title = "Monitor"
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
            else -> {
                supportActionBar?.title = "GesPay Admin"
                supportActionBar?.setDisplayHomeAsUpEnabled(false)
            }
        }
    }

    private fun setupBackPressedHandler() {
        onBackPressedDispatcher.addCallback(this, onBackPressedCallback)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.main_menu, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_logout -> {
                showLogoutConfirmation()
                true
            }
            R.id.action_debug_info -> {
                showDebugInfo()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun handleBackNavigation() {
        when (navController.currentDestination?.id) {
            R.id.homeFragment -> {
                showExitConfirmation()
            }
            R.id.historyFragment, R.id.monitorFragment -> {
                try {
                    navController.navigate(R.id.homeFragment)
                } catch (e: Exception) {
                    Log.e(TAG, "❌ Navigation error", e)
                    showExitConfirmation()
                }
            }
            else -> {
                if (!navController.popBackStack()) {
                    try {
                        navController.navigate(R.id.homeFragment)
                    } catch (e: Exception) {
                        Log.e(TAG, "❌ Navigation fallback error", e)
                        showExitConfirmation()
                    }
                }
            }
        }
    }

    private fun showExitConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Exit Application")
            .setMessage("Are you sure you want to exit GesPay Admin?")
            .setPositiveButton("Yes") { _, _ ->
                Log.d(TAG, "📱 User confirmed app exit")
                finish()
            }
            .setNegativeButton("Cancel") { _, _ ->
                Log.d(TAG, "📱 User cancelled app exit")
            }
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Yes") { _, _ ->
                Log.d(TAG, "🚪 User confirmed logout")
                viewModel.logout()
            }
            .setNegativeButton("Cancel") { _, _ ->
                Log.d(TAG, "📱 User cancelled logout")
            }
            .show()
    }

    private fun showDebugInfo() {
        val debugInfo = """
            📱 SIMPLIFIED GESPAY ADMIN DEBUG INFO:
            Current Destination: ${navController.currentDestination?.label}
            Session Info: ${viewModel.getSessionDebugInfo()}
            Feature Flags: REMOVED - All features enabled
            
            🔄 REFACTORING STATUS:
            ✅ FeatureFlags eliminated
            ✅ Receipt/TransactionLog data structure
            ✅ New API endpoints integration
            ✅ Simplified codebase
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
                        Log.e(TAG, "❌ Session error: ${resource.message}")
                        handleSessionExpired()
                    }
                    is Resource.Success -> {
                        Log.d(TAG, "✅ Session valid")
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
                        Log.w(TAG, "⚠️ Session expired event received")
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
            .setTitle("Session Expired")
            .setMessage("Your session has expired. Please login again.")
            .setPositiveButton("Login") { _, _ ->
                Log.d(TAG, "📱 Redirecting to login after session expiry")
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
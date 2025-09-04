// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/TransactionLogActivity.kt - PHASE 3 SIMPLIFIED
package com.proyek.maganggsp.presentation.detail_loket

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ActivityDetailLoketBinding
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.util.AppUtils
import com.proyek.maganggsp.util.NavigationConstants
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class TransactionLogActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailLoketBinding
    private val viewModel: TransactionLogViewModel by viewModels()
    private lateinit var transactionLogAdapter: TransactionLogAdapter

    companion object {
        private const val TAG = "TransactionLogActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "🔄 PHASE 3: TransactionLogActivity - FeatureFlags REMOVED")

        binding = ActivityDetailLoketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupToolbar()
        setupRecyclerView()
        setupActionListeners()
        observeStates()
    }

    private fun setupUI() {
        // ✅ PHASE 3: Simplified UI setup - no feature flag conditions
        Log.d(TAG, "🎨 Setting up UI - all features enabled")

        // All components are always visible and functional
        binding.mutationsShimmerLayout.isVisible = true
        binding.rvMutations.isVisible = true
        binding.btnBlock.isVisible = true
        binding.btnUnblock.isVisible = true
        binding.btnClearFlags.isVisible = false // Keep hidden for now

        // Update labels for Transaction Log context
        binding.tvMutasiTitle.text = "Transaction Logs"
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            Log.d(TAG, "📱 Back navigation triggered")
        }
    }

    private fun setupRecyclerView() {
        // ✅ PHASE 3: Always setup RecyclerView - no conditional logic
        transactionLogAdapter = TransactionLogAdapter()
        binding.rvMutations.apply {
            adapter = transactionLogAdapter
            layoutManager = LinearLayoutManager(this@TransactionLogActivity)
            isNestedScrollingEnabled = false
        }

        // Optional: Add click listener for transaction items
        transactionLogAdapter.setOnItemClickListener { transactionLog ->
            showTransactionDetails(transactionLog)
        }

        Log.d(TAG, "✅ TransactionLog RecyclerView setup completed")
    }

    private fun setupActionListeners() {
        // ✅ PHASE 3: Always setup listeners - no feature flag conditions
        binding.btnBlock.setOnClickListener {
            Log.d(TAG, "🚫 Block action requested")
            showActionNotImplemented("Block Profile")
        }

        binding.btnUnblock.setOnClickListener {
            Log.d(TAG, "✅ Unblock action requested")
            showActionNotImplemented("Unblock Profile")
        }

        binding.btnClearFlags.setOnClickListener {
            Log.d(TAG, "🧹 Clear flags action requested")
            showActionNotImplemented("Clear Flags")
        }
    }

    private fun showActionNotImplemented(actionName: String) {
        Toast.makeText(this, "$actionName: Coming soon in future release", Toast.LENGTH_SHORT).show()
    }

    private fun showTransactionDetails(transactionLog: TransactionLog) {
        val details = """
        Transaction Details:
        
        Reference: ${transactionLog.tldRefnum}
        PAN: ${transactionLog.tldPan}
        ID Pelanggan: ${transactionLog.tldIdpel}
        Amount: ${transactionLog.getFormattedAmount()}
        Balance: ${transactionLog.getFormattedBalance()}
        Date: ${transactionLog.getFormattedDate()}
        PPID: ${transactionLog.tldPpid}
        """.trimIndent()

        android.app.AlertDialog.Builder(this)
            .setTitle("Transaction Details")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()
    }

    private fun observeStates() {
        // ✅ PHASE 3: Simplified state observation - no conditional observers

        // Observe transaction logs
        lifecycleScope.launch {
            viewModel.transactionLogsState.collectLatest { resource ->
                handleTransactionLogsState(resource)
            }
        }

        // Observe action state
        lifecycleScope.launch {
            viewModel.actionState.collectLatest { resource ->
                handleActionState(resource)
            }
        }

        // Observe events
        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest { event ->
                handleViewModelEvents(event)
            }
        }
    }

    private fun handleTransactionLogsState(resource: Resource<List<TransactionLog>>) {
        // ✅ PHASE 3: Use AppUtils for consistent state management
        AppUtils.handleLoadingState(
            shimmerView = binding.mutationsShimmerLayout,
            contentView = binding.rvMutations,
            emptyView = binding.tvMutationsError,
            resource = resource
        )

        when (resource) {
            is Resource.Success -> {
                val data = resource.data ?: emptyList()
                Log.d(TAG, "✅ Transaction logs loaded: ${data.size} items")

                if (data.isEmpty()) {
                    binding.tvMutationsError.text = "No transaction logs available for this profile."
                } else {
                    transactionLogAdapter.updateData(data)
                    updateProfileInfo(data.firstOrNull()) // Use first transaction for profile context
                }
            }
            is Resource.Error -> {
                Log.e(TAG, "❌ Transaction logs error: ${resource.message}")
                binding.tvMutationsError.text = "Failed to load transaction logs.\nPull down to refresh."
                AppUtils.showError(this, resource.exception)
            }
            is Resource.Loading -> {
                Log.d(TAG, "⏳ Loading transaction logs...")
            }
            else -> Unit
        }
    }

    private fun handleActionState(resource: Resource<Unit>) {
        binding.mainProgressBar.isVisible = resource is Resource.Loading
        setButtonsEnabled(resource !is Resource.Loading)

        when (resource) {
            is Resource.Error -> {
                AppUtils.showError(this, resource.exception)
                viewModel.onActionConsumed()
                Log.e(TAG, "❌ Action error: ${resource.message}")
            }
            is Resource.Success -> {
                Toast.makeText(this, "Action completed successfully", Toast.LENGTH_SHORT).show()
                viewModel.onActionConsumed()
                Log.d(TAG, "✅ Action completed successfully")
            }
            else -> Unit
        }
    }

    private fun handleViewModelEvents(event: TransactionLogViewModel.UiEvent) {
        when (event) {
            is TransactionLogViewModel.UiEvent.ShowToast -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()
                Log.d(TAG, "📢 Toast event: ${event.message}")
            }
            is TransactionLogViewModel.UiEvent.NavigateBack -> {
                finish()
            }
        }
    }

    private fun setButtonsEnabled(isEnabled: Boolean) {
        binding.btnBlock.isEnabled = isEnabled
        binding.btnUnblock.isEnabled = isEnabled
        Log.d(TAG, "🔘 Buttons enabled state: $isEnabled")
    }

    private fun updateProfileInfo(transactionLog: TransactionLog?) {
        if (transactionLog == null) {
            Log.w(TAG, "⚠️ No transaction data available for profile info")
            return
        }

        // ✅ PHASE 3: Update UI with TransactionLog data (repurposed from old Loket fields)
        binding.tvLoketId.text = "PPID: ${transactionLog.tldPpid}"
        binding.tvLoketName.text = "Profile ${transactionLog.tldIdpel}"
        binding.tvPhoneValue.text = "PAN: ${transactionLog.tldPan}"
        binding.tvEmailValue.text = "Balance: ${transactionLog.getFormattedBalance()}"

        // Set default status (since we don't have status in TransactionLog)
        binding.chipStatus.text = "Active"
        binding.chipStatus.setChipBackgroundColor(
            ContextCompat.getColorStateList(this, R.color.chip_normal_background)
        )

        Log.d(TAG, "🔄 Profile info updated from transaction data")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🧹 TransactionLogActivity destroyed")
    }
}
// File: app/src/main/java/com/proyek/maganggsp/presentation/transaction/TransactionLogActivity.kt - FIXED
package com.proyek.maganggsp.presentation.transaction

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.proyek.maganggsp.databinding.ActivityTransactionLogBinding
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.presentation.adapters.TransactionLogAdapter
import com.proyek.maganggsp.util.NavigationConstants
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.AppUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * FIXED: TransactionLog detail activity dengan proper ViewBinding
 * Shows transaction logs dari /trx/ppid/{ppid} API
 * Navigation: Receipt click -> TransactionLogActivity
 */
@AndroidEntryPoint
class TransactionLogActivity : AppCompatActivity() {

    // FIXED: Proper ViewBinding declaration
    private lateinit var binding: ActivityTransactionLogBinding
    private val viewModel: TransactionLogViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionLogAdapter

    companion object {
        private const val TAG = "TransactionLogActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // FIXED: Proper ViewBinding initialization
        binding = ActivityTransactionLogBinding.inflate(layoutInflater)
        setContentView(binding.root)

        AppUtils.logInfo(TAG, "TransactionLog screen created")

        setupUI()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()
    }

    private fun setupUI() {
        // Toolbar setup
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.title = "Log Transaksi"
        binding.toolbar.subtitle = "PPID: ${viewModel.getCurrentPpid()}"

        // Get PPID from intent
        val ppid = intent.getStringExtra(NavigationConstants.ARG_PPID) ?: ""
        AppUtils.logInfo(TAG, "TransactionLog screen opened for PPID: $ppid")
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionLogAdapter()
        binding.rvTransactionLogs.apply {
            layoutManager = LinearLayoutManager(this@TransactionLogActivity)
            adapter = transactionAdapter
        }
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.transactionLogsState.collect { resource ->
                handleTransactionLogsResource(resource)
            }
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                handleViewModelEvents(event)
            }
        }
    }

    private fun setupClickListeners() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshData()
        }
    }

    private fun handleTransactionLogsResource(resource: Resource<List<TransactionLog>>) {
        // Apply unified loading state management via bridge
        AppUtils.handleLoadingState(
            resource = resource,
            shimmerView = binding.shimmerLayout,
            contentView = binding.rvTransactionLogs,
            emptyView = binding.tvEmpty
        )

        binding.swipeRefreshLayout.isRefreshing = false

        when (resource) {
            is Resource.Success -> {
                transactionAdapter.updateTransactions(resource.data)
                AppUtils.logInfo(TAG, "Transaction logs loaded: ${resource.data.size} transactions")

                if (resource.data.isEmpty()) {
                    AppUtils.applyEmptyState(
                        textView = binding.tvEmpty,
                        context = "transactions",
                        itemCount = 0
                    )
                }

                // Update stats display
                updateTransactionStats(resource.data)
            }
            is Resource.Error -> {
                AppUtils.showError(this, resource.exception)
                AppUtils.logError(TAG, "Transaction logs error", resource.exception)
                AppUtils.applyEmptyState(
                    textView = binding.tvEmpty,
                    context = "transactions",
                    itemCount = 0
                )
            }
            is Resource.Loading -> {
                AppUtils.logDebug(TAG, "Loading transaction logs...")
            }
            is Resource.Empty -> {
                AppUtils.applyEmptyState(
                    textView = binding.tvEmpty,
                    context = "transactions",
                    itemCount = 0
                )
            }
        }
    }

    private fun updateTransactionStats(transactions: List<TransactionLog>) {
        val stats = viewModel.getTransactionStats()
        if (stats != null) {
            binding.toolbar.subtitle = "PPID: ${viewModel.getCurrentPpid()} | ${stats.totalTransactions} transaksi"
        }
    }

    private fun handleViewModelEvents(event: TransactionLogViewModel.UiEvent) {
        when (event) {
            is TransactionLogViewModel.UiEvent.ShowToast -> {
                AppUtils.showError(this, event.message)
            }
            is TransactionLogViewModel.UiEvent.NavigateBack -> {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        AppUtils.logInfo(TAG, "TransactionLog screen destroyed")
    }
}
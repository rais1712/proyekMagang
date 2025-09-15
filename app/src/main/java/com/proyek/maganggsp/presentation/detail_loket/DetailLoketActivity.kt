// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/DetailLoketActivity.kt - MVP CORE
package com.proyek.maganggsp.presentation.detail_loket

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ActivityDetailLoketBinding
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.LoketStatus
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.presentation.adapters.TransactionLogAdapter
import com.proyek.maganggsp.util.NavigationConstants
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.AppUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * MVP CORE: Detail Loket Activity dengan block/unblock functionality
 */
@AndroidEntryPoint
class DetailLoketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailLoketBinding
    private val viewModel: DetailLoketViewModel by viewModels()
    private lateinit var transactionAdapter: TransactionLogAdapter

    private var currentPpid: String = ""

    companion object {
        private const val TAG = "DetailLoketActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLoketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        extractArguments()
        setupUI()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        if (currentPpid.isNotEmpty()) {
            viewModel.loadLoketProfile(currentPpid)
        } else {
            AppUtils.showError(this, "PPID tidak valid")
            finish()
        }
    }

    private fun extractArguments() {
        // Extract PPID from multiple possible sources
        currentPpid = intent.getStringExtra(NavigationConstants.ARG_PPID)
            ?: intent.getBundleExtra("android:support:navigation:fragment:args")?.getString(NavigationConstants.ARG_PPID)
                    ?: intent.getBundleExtra("bundle")?.getString(NavigationConstants.ARG_PPID)
                    ?: ""

        Log.d(TAG, "Extracted PPID: $currentPpid")
        AppUtils.logInfo(TAG, "Loading detail for PPID: $currentPpid")
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
    }

    private fun setupRecyclerView() {
        transactionAdapter = TransactionLogAdapter()
        binding.rvMutations.apply {
            layoutManager = LinearLayoutManager(this@DetailLoketActivity)
            adapter = transactionAdapter
        }
    }

    private fun setupObservers() {
        // Loket profile observer
        lifecycleScope.launch {
            viewModel.loketProfile.collect { resource ->
                handleLoketProfileResource(resource)
            }
        }

        // Transaction logs observer
        lifecycleScope.launch {
            viewModel.transactionLogs.collect { resource ->
                handleTransactionLogsResource(resource)
            }
        }

        // Block/Unblock action observer
        lifecycleScope.launch {
            viewModel.blockUnblockResult.collect { resource ->
                handleBlockUnblockResult(resource)
            }
        }
    }

    private fun setupClickListeners() {
        binding.btnBlock.setOnClickListener {
            showBlockConfirmation()
        }

        binding.btnUnblock.setOnClickListener {
            showUnblockConfirmation()
        }

        binding.btnDiblokir.setOnClickListener {
            AppUtils.showSuccess(this, "Loket ini sudah diblokir")
        }

        binding.btnClearFlags.setOnClickListener {
            // Future implementation for clearing flags
            AppUtils.showSuccess(this, "Fitur hapus penanda akan segera hadir")
        }
    }

    private fun handleLoketProfileResource(resource: Resource<Loket>) {
        // Apply to dual loading (card + transactions)
        resource.applyToDualLoadingViews(
            primaryShimmer = binding.shimmerCardInfo,
            primaryContent = binding.cardLoketInfo,
            secondaryShimmer = binding.mutationsShimmerLayout,
            secondaryContent = binding.frameLayoutMutations,
            emptyView = null
        )

        when (resource) {
            is Resource.Success -> {
                displayLoketProfile(resource.data)
                // Auto-load transaction logs
                viewModel.loadTransactionLogs(currentPpid)
            }
            is Resource.Error -> {
                AppUtils.showError(this, resource.exception)
                AppUtils.logError(TAG, "Loket profile load error", resource.exception)
            }
            is Resource.Loading -> {
                AppUtils.logDebug(TAG, "Loading loket profile...")
            }
            is Resource.Empty -> {
                AppUtils.showError(this, "Data loket tidak ditemukan")
            }
        }
    }

    private fun handleTransactionLogsResource(resource: Resource<List<TransactionLog>>) {
        when (resource) {
            is Resource.Success -> {
                transactionAdapter.submitList(resource.data)
                binding.mutationsShimmerLayout.visibility = View.GONE
                binding.rvMutations.visibility = View.VISIBLE
                binding.tvMutationsError.visibility = View.GONE
                AppUtils.logInfo(TAG, "Transaction logs loaded: ${resource.data.size}")
            }
            is Resource.Error -> {
                binding.mutationsShimmerLayout.visibility = View.GONE
                binding.rvMutations.visibility = View.GONE
                binding.tvMutationsError.visibility = View.VISIBLE
                binding.tvMutationsError.text = "Gagal memuat log transaksi"
                AppUtils.logError(TAG, "Transaction logs error", resource.exception)
            }
            is Resource.Loading -> {
                binding.mutationsShimmerLayout.visibility = View.VISIBLE
                binding.rvMutations.visibility = View.GONE
                binding.tvMutationsError.visibility = View.GONE
            }
            is Resource.Empty -> {
                binding.mutationsShimmerLayout.visibility = View.GONE
                binding.rvMutations.visibility = View.GONE
                binding.tvMutationsError.visibility = View.VISIBLE
                binding.tvMutationsError.text = "Belum ada log transaksi"
            }
        }
    }

    private fun handleBlockUnblockResult(resource: Resource<Unit>) {
        when (resource) {
            is Resource.Success -> {
                AppUtils.showSuccess(this, "Status loket berhasil diupdate")
                // Reload profile to get updated status
                viewModel.loadLoketProfile(currentPpid)
            }
            is Resource.Error -> {
                AppUtils.showError(this, resource.exception)
                AppUtils.logError(TAG, "Block/Unblock error", resource.exception)
            }
            is Resource.Loading -> {
                // Show loading state on buttons
                binding.btnBlock.isEnabled = false
                binding.btnUnblock.isEnabled = false
                binding.btnDiblokir.isEnabled = false
            }
            is Resource.Empty -> { /* Not applicable */ }
        }
    }

    private fun displayLoketProfile(loket: Loket) {
        with(binding) {
            // Basic info
            tvLoketId.text = loket.ppid
            tvLoketName.text = loket.namaLoket
            tvPhoneValue.text = formatPhoneDisplay(loket.nomorHP)
            tvEmailValue.text = loket.email

            // Status chip and styling
            updateStatusDisplay(loket.status)

            // Button states
            updateButtonStates(loket.status)

            AppUtils.logInfo(TAG, "Displayed profile for: ${loket.namaLoket} (${loket.status})")
        }
    }

    private fun formatPhoneDisplay(phone: String): String {
        return when {
            phone.startsWith("+62") -> phone
            phone.startsWith("08") -> "+62${phone.substring(1)}"
            phone.startsWith("62") -> "+$phone"
            else -> phone
        }
    }

    private fun updateStatusDisplay(status: LoketStatus) {
        with(binding) {
            when (status) {
                LoketStatus.NORMAL -> {
                    chipStatus.text = "Normal"
                    chipStatus.setChipBackgroundColorResource(R.color.chip_normal_background)
                    layoutLoketInfo.setBackgroundResource(R.drawable.bg_card_info_normal)
                }
                LoketStatus.BLOCKED -> {
                    chipStatus.text = "Diblokir"
                    chipStatus.setChipBackgroundColorResource(R.color.chip_blocked_background)
                    layoutLoketInfo.setBackgroundResource(R.drawable.bg_card_info_diblokir)
                }
                LoketStatus.FLAGGED -> {
                    chipStatus.text = "Ditandai"
                    chipStatus.setChipBackgroundColorResource(R.color.chip_flagged_background)
                    layoutLoketInfo.setBackgroundResource(R.drawable.bg_card_info_ditandai)
                }
            }
        }
    }

    private fun updateButtonStates(status: LoketStatus) {
        with(binding) {
            // Reset all buttons enabled state
            btnBlock.isEnabled = true
            btnUnblock.isEnabled = true
            btnDiblokir.isEnabled = true

            when (status) {
                LoketStatus.NORMAL -> {
                    btnBlock.visibility = View.VISIBLE
                    btnUnblock.visibility = View.GONE
                    btnDiblokir.visibility = View.GONE
                    btnClearFlags.visibility = View.GONE
                }
                LoketStatus.BLOCKED -> {
                    btnBlock.visibility = View.GONE
                    btnUnblock.visibility = View.VISIBLE
                    btnDiblokir.visibility = View.GONE
                    btnClearFlags.visibility = View.GONE
                }
                LoketStatus.FLAGGED -> {
                    btnBlock.visibility = View.VISIBLE
                    btnUnblock.visibility = View.GONE
                    btnDiblokir.visibility = View.GONE
                    btnClearFlags.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun showBlockConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Konfirmasi Blokir")
            .setMessage("Yakin ingin memblokir loket ini?")
            .setPositiveButton("Ya") { _, _ ->
                viewModel.blockLoket(currentPpid)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showUnblockConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Konfirmasi Buka Blokir")
            .setMessage("Yakin ingin membuka blokir loket ini?")
            .setPositiveButton("Ya") { _, _ ->
                viewModel.unblockLoket(currentPpid)
            }
            .setNegativeButton("Tidak", null)
            .show()
    }
}
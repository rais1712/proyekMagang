// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/DetailLoketActivity.kt - PPID ARGS FIXED
package com.proyek.maganggsp.presentation.detail_loket

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
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
import com.proyek.maganggsp.util.extractPpidSafely
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * FIXED: DetailLoketActivity dengan proper PPID argument extraction dan real API integration
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
            AppUtils.logInfo(TAG, "Loading detail for PPID: $currentPpid")
            viewModel.loadLoketProfile(currentPpid)
        } else {
            AppUtils.showError(this, "PPID tidak valid")
            AppUtils.logError(TAG, "Invalid PPID, finishing activity")
            finish()
        }
    }

    private fun extractArguments() {
        // FIXED: Robust PPID extraction dari multiple sources
        currentPpid = when {
            // Navigation arguments (primary)
            intent.getStringExtra(NavigationConstants.ARG_PPID) != null -> {
                intent.getStringExtra(NavigationConstants.ARG_PPID)!!
            }

            // Bundle arguments (Navigation Component)
            intent.getBundleExtra("android:support:navigation:fragment:args")?.getString(NavigationConstants.ARG_PPID) != null -> {
                intent.getBundleExtra("android:support:navigation:fragment:args")!!.getString(NavigationConstants.ARG_PPID)!!
            }

            // Extra bundle
            intent.getBundleExtra("bundle")?.getString(NavigationConstants.ARG_PPID) != null -> {
                intent.getBundleExtra("bundle")!!.getString(NavigationConstants.ARG_PPID)!!
            }

            // Legacy support
            intent.getStringExtra("noLoket") != null -> {
                intent.getStringExtra("noLoket")!!
            }

            else -> ""
        }

        // ENHANCED: Safe PPID extraction dengan fallback
        currentPpid = currentPpid.extractPpidSafely()

        Log.d(TAG, "✅ Final extracted PPID: $currentPpid")
        AppUtils.logInfo(TAG, "Extracted PPID for detail view: $currentPpid")
    }

    private fun setupUI() {
        // Toolbar navigation
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // Update title dengan PPID info
        binding.toolbar.title = "Detail Loket"
        binding.toolbar.subtitle = currentPpid.takeIf { it.isNotBlank() }
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
            AppUtils.showSuccess(this, "Fitur hapus penanda akan segera hadir")
        }

        // Swipe refresh untuk reload data
        binding.nestedScrollView.setOnRefreshListener {
            viewModel.refresh(currentPpid)
        }
    }

    private fun handleLoketProfileResource(resource: Resource<Loket>) {
        // Apply to dual loading (card + transactions shimmer)
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
                // Auto-load transaction logs after profile loads
                viewModel.loadTransactionLogs(currentPpid)
                AppUtils.logInfo(TAG, "Loket profile loaded: ${resource.data.namaLoket}")
            }
            is Resource.Error -> {
                AppUtils.showError(this, resource.exception)
                AppUtils.logError(TAG, "Loket profile load error", resource.exception)

                // Show retry option
                binding.cardLoketInfo.visibility = View.GONE
                showRetryOptions()
            }
            is Resource.Loading -> {
                AppUtils.logDebug(TAG, "Loading loket profile...")
            }
            is Resource.Empty -> {
                AppUtils.showError(this, "Data loket tidak ditemukan")
                showRetryOptions()
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
                AppUtils.logInfo(TAG, "Transaction logs loaded: ${resource.data.size} transactions")

                // Update empty state jika tidak ada transaksi
                if (resource.data.isEmpty()) {
                    binding.tvMutationsError.visibility = View.VISIBLE
                    binding.tvMutationsError.text = "Belum ada log transaksi untuk loket ini"
                }
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
                binding.tvMutationsError.text = getString(R.string.empty_no_transactions)
            }
        }
    }

    private fun handleBlockUnblockResult(resource: Resource<Unit>) {
        when (resource) {
            is Resource.Success -> {
                AppUtils.showSuccess(this, "Status loket berhasil diupdate")
                // Reload profile to get updated status
                viewModel.loadLoketProfile(currentPpid)

                // Reset button states
                resetButtonStates()
            }
            is Resource.Error -> {
                AppUtils.showError(this, resource.exception)
                AppUtils.logError(TAG, "Block/Unblock error", resource.exception)

                // Reset button states
                resetButtonStates()
            }
            is Resource.Loading -> {
                // Show loading state on buttons
                setButtonsLoadingState(true)
            }
            is Resource.Empty -> {
                resetButtonStates()
            }
        }
    }

    private fun displayLoketProfile(loket: Loket) {
        with(binding) {
            // Basic info
            tvLoketId.text = loket.ppid
            tvLoketName.text = loket.namaLoket
            tvPhoneValue.text = formatPhoneDisplay(loket.nomorHP)
            tvEmailValue.text = loket.email.takeIf { it.isNotBlank() } ?: "Email tidak tersedia"

            // Status chip dan styling
            updateStatusDisplay(loket.status)

            // Button states berdasarkan status
            updateButtonStates(loket.status)

            AppUtils.logInfo(TAG, "Displayed profile for: ${loket.namaLoket} (${loket.status})")
        }
    }

    private fun formatPhoneDisplay(phone: String): String {
        return when {
            phone.startsWith("+62") -> phone
            phone.startsWith("08") -> "+62${phone.substring(1)}"
            phone.startsWith("62") -> "+$phone"
            phone.isNotBlank() -> phone
            else -> "No. HP tidak tersedia"
        }
    }

    private fun updateStatusDisplay(status: LoketStatus) {
        with(binding) {
            when (status) {
                LoketStatus.NORMAL -> {
                    chipStatus.text = getString(R.string.status_normal)
                    chipStatus.setChipBackgroundColorResource(R.color.chip_normal_background)
                    layoutLoketInfo.setBackgroundResource(R.drawable.bg_card_info_normal)
                }
                LoketStatus.BLOCKED -> {
                    chipStatus.text = getString(R.string.status_blocked)
                    chipStatus.setChipBackgroundColorResource(R.color.chip_blocked_background)
                    layoutLoketInfo.setBackgroundResource(R.drawable.bg_card_info_diblokir)
                }
                LoketStatus.FLAGGED -> {
                    chipStatus.text = getString(R.string.status_flagged)
                    chipStatus.setChipBackgroundColorResource(R.color.chip_flagged_background)
                    layoutLoketInfo.setBackgroundResource(R.drawable.bg_card_info_ditandai)
                }
            }
        }
    }

    private fun updateButtonStates(status: LoketStatus) {
        with(binding) {
            // Reset visibility
            btnBlock.visibility = View.GONE
            btnUnblock.visibility = View.GONE
            btnDiblokir.visibility = View.GONE
            btnClearFlags.visibility = View.GONE

            when (status) {
                LoketStatus.NORMAL -> {
                    btnBlock.visibility = View.VISIBLE
                }
                LoketStatus.BLOCKED -> {
                    btnUnblock.visibility = View.VISIBLE
                }
                LoketStatus.FLAGGED -> {
                    btnBlock.visibility = View.VISIBLE
                    btnClearFlags.visibility = View.VISIBLE
                }
            }
        }
    }

    private fun setButtonsLoadingState(isLoading: Boolean) {
        with(binding) {
            btnBlock.isEnabled = !isLoading
            btnUnblock.isEnabled = !isLoading
            btnDiblokir.isEnabled = !isLoading
            btnClearFlags.isEnabled = !isLoading

            if (isLoading) {
                btnBlock.text = getString(R.string.action_processing)
                btnUnblock.text = getString(R.string.action_processing)
            } else {
                btnBlock.text = getString(R.string.block_loket)
                btnUnblock.text = getString(R.string.unblock_loket)
            }
        }
    }

    private fun resetButtonStates() {
        with(binding) {
            btnBlock.isEnabled = true
            btnUnblock.isEnabled = true
            btnDiblokir.isEnabled = true
            btnClearFlags.isEnabled = true

            btnBlock.text = getString(R.string.block_loket)
            btnUnblock.text = getString(R.string.unblock_loket)
        }
    }

    private fun showBlockConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_confirm_block_title))
            .setMessage(getString(R.string.dialog_confirm_block_message))
            .setPositiveButton(getString(R.string.dialog_button_yes)) { _, _ ->
                viewModel.blockLoket(currentPpid)
                AppUtils.logInfo(TAG, "User confirmed block loket: $currentPpid")
            }
            .setNegativeButton(getString(R.string.dialog_button_no), null)
            .show()
    }

    private fun showUnblockConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(getString(R.string.dialog_confirm_unblock_title))
            .setMessage(getString(R.string.dialog_confirm_unblock_message))
            .setPositiveButton(getString(R.string.dialog_button_yes)) { _, _ ->
                viewModel.unblockLoket(currentPpid)
                AppUtils.logInfo(TAG, "User confirmed unblock loket: $currentPpid")
            }
            .setNegativeButton(getString(R.string.dialog_button_no), null)
            .show()
    }

    private fun showRetryOptions() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Gagal Memuat Data")
            .setMessage("Tidak dapat memuat data loket. Coba lagi?")
            .setPositiveButton(getString(R.string.retry)) { _, _ ->
                viewModel.refresh(currentPpid)
            }
            .setNegativeButton(getString(R.string.close)) { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    override fun onResume() {
        super.onResume()
        AppUtils.logDebug(TAG, "DetailLoketActivity resumed for PPID: $currentPpid")
    }

    override fun onDestroy() {
        super.onDestroy()
        AppUtils.logDebug(TAG, "DetailLoketActivity destroyed")
    }
}
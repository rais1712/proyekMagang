package com.proyek.maganggsp.presentation.detail_loket

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ActivityDetailLoketBinding
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.model.LoketStatus
import com.proyek.maganggsp.presentation.adapters.ReceiptAdapter
import com.proyek.maganggsp.presentation.transaction.TransactionLogActivity
import com.proyek.maganggsp.util.NavigationConstants
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.LoggingUtils
import com.proyek.maganggsp.util.ErrorHandler
import com.proyek.maganggsp.util.UiStateHelper
import com.proyek.maganggsp.util.extractPpidSafely
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * DetailLoketActivity menampilkan informasi profil dan daftar receipt
 */
@AndroidEntryPoint
class DetailLoketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailLoketBinding
    private val viewModel: DetailLoketViewModel by viewModels()
    private lateinit var receiptAdapter: ReceiptAdapter

    private var currentPpid: String = ""

    companion object {
        private const val TAG = "DetailLoketActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLoketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LoggingUtils.logInfo(TAG, "DetailLoketActivity created")

        extractArguments()
        setupUI()
        setupRecyclerView()
        setupObservers()
        setupClickListeners()

        if (currentPpid.isNotEmpty()) {
            LoggingUtils.logInfo(TAG, "Loading detail for PPID: $currentPpid")
        } else {
            ErrorHandler.showError(this, "PPID tidak valid")
            LoggingUtils.logError(TAG, "Invalid PPID, finishing activity")
            finish()
        }
    }

    private fun extractArguments() {
        currentPpid = intent.getStringExtra(NavigationConstants.ARG_PPID)
            ?: intent.getBundleExtra("android:support:navigation:fragment:args")?.getString(NavigationConstants.ARG_PPID)
            ?: intent.getStringExtra("ppid")
            ?: ""

        currentPpid = currentPpid.extractPpidSafely()

        LoggingUtils.logInfo(TAG, "Extracted PPID: $currentPpid")
    }

    private fun setupUI() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.toolbar.title = "Detail Loket"
        binding.toolbar.subtitle = currentPpid.takeIf { it.isNotBlank() }
    }

    private fun setupRecyclerView() {
        receiptAdapter = ReceiptAdapter { receipt ->
            navigateToTransactionLog(receipt.ppid)
        }

        binding.rvMutations.apply {
            layoutManager = LinearLayoutManager(this@DetailLoketActivity)
            adapter = receiptAdapter
        }

        binding.tvMutasiTitle.text = "Receipts"
    }

    private fun setupObservers() {
        lifecycleScope.launch {
            viewModel.profileState.collect { resource ->
                handleProfileResource(resource)
            }
        }

        lifecycleScope.launch {
            viewModel.actionState.collect { resource ->
                handleActionResult(resource)
            }
        }

        lifecycleScope.launch {
            viewModel.eventFlow.collect { event ->
                handleViewModelEvents(event)
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
            ErrorHandler.showSuccess(this, "Profil ini sudah diblokir")
        }

        binding.btnClearFlags.setOnClickListener {
            ErrorHandler.showSuccess(this, "Fitur hapus penanda akan segera hadir")
        }
    }

    private fun handleProfileResource(resource: Resource<Receipt>) {
        UiStateHelper.handleDualLoadingState(
            resource = resource,
            primaryShimmer = binding.shimmerCardInfo,
            primaryContent = binding.cardLoketInfo,
            secondaryShimmer = binding.mutationsShimmerLayout,
            secondaryContent = binding.frameLayoutMutations
        )

        when (resource) {
            is Resource.Success -> {
                displayProfileInfo(resource.data)
                receiptAdapter.updateReceipts(listOf(resource.data))
                LoggingUtils.logInfo(TAG, "Profile loaded: ${resource.data.namaLoket}")
            }
            is Resource.Error -> {
                ErrorHandler.showError(this, resource.message)
                LoggingUtils.logError(TAG, "Profile load error", resource.message)
                showRetryOptions()
            }
            is Resource.Loading -> {
                LoggingUtils.logDebug(TAG, "Loading profile...")
            }
            is Resource.Empty() -> {
                ErrorHandler.showError(this, "Data profil tidak ditemukan")
                showRetryOptions()
            }
        }
    }

    private fun displayProfileInfo(receipt: Receipt) {
        with(binding) {
            tvLoketId.text = receipt.ppid
            tvLoketName.text = receipt.namaLoket.takeIf { it.isNotBlank() } ?: "Receipt ${receipt.refNumber}"
            tvPhoneValue.text = receipt.nomorHP
            tvEmailValue.text = receipt.email.takeIf { it.isNotBlank() } ?: "Email tidak tersedia"

            val status = LoketStatus.fromPpid(receipt.ppid)
            updateStatusDisplay(status)
            updateButtonStates(status)

            LoggingUtils.logInfo(TAG, "Displayed profile for: ${receipt.namaLoket} (${status})")
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

    private fun handleActionResult(resource: Resource<Unit>) {
        when (resource) {
            is Resource.Success -> {
                ErrorHandler.showSuccess(this, "Operasi berhasil")
                viewModel.refreshData()
                resetButtonStates()
            }
            is Resource.Error -> {
                ErrorHandler.showError(this, resource.message)
                LoggingUtils.logError(TAG, "Action error", resource.message)
                resetButtonStates()
            }
            is Resource.Loading -> {
                setButtonsLoadingState(true)
            }
            is Resource.Empty() -> {
                resetButtonStates()
            }
        }
    }

    private fun handleViewModelEvents(event: DetailLoketViewModel.UiEvent) {
        when (event) {
            is DetailLoketViewModel.UiEvent.ShowToast -> {
                ErrorHandler.showSuccess(this, event.message)
            }
            is DetailLoketViewModel.UiEvent.NavigateToTransactionLog -> {
                navigateToTransactionLog(event.ppid)
            }
            is DetailLoketViewModel.UiEvent.NavigateBack -> {
                onBackPressedDispatcher.onBackPressed()
            }
        }
    }

    private fun navigateToTransactionLog(ppid: String) {
        try {
            val intent = Intent(this, TransactionLogActivity::class.java).apply {
                putExtra(NavigationConstants.ARG_PPID, ppid)
            }
            startActivity(intent)
            LoggingUtils.logInfo(TAG, "Navigating to TransactionLog with PPID: $ppid")
        } catch (e: Exception) {
            LoggingUtils.logError(TAG, "Navigation error", e)
            ErrorHandler.showError(this, "Gagal membuka log transaksi")
        }
    }

    private fun setButtonsLoadingState(isLoading: Boolean) {
        with(binding) {
            btnBlock.isEnabled = !isLoading
            btnUnblock.isEnabled = !isLoading
            btnDiblokir.isEnabled = !isLoading
            btnClearFlags.isEnabled = !isLoading

            if (isLoading) {
                btnBlock.text = "Memproses..."
                btnUnblock.text = "Memproses..."
            } else {
                resetButtonStates()
            }
        }
    }

    private fun resetButtonStates() {
        with(binding) {
            btnBlock.isEnabled = true
            btnUnblock.isEnabled = true
            btnDiblokir.isEnabled = true
            btnClearFlags.isEnabled = true

            btnBlock.text = "Blokir Loket"
            btnUnblock.text = "Buka Blokir"
        }
    }

    private fun showBlockConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Konfirmasi Blokir")
            .setMessage("Yakin ingin memblokir profil ini?")
            .setPositiveButton("Ya") { _, _ ->
                viewModel.blockProfile()
                LoggingUtils.logInfo(TAG, "User confirmed block profile: $currentPpid")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showUnblockConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Konfirmasi Buka Blokir")
            .setMessage("Yakin ingin membuka blokir profil ini?")
            .setPositiveButton("Ya") { _, _ ->
                viewModel.unblockProfile()
                LoggingUtils.logInfo(TAG, "User confirmed unblock profile: $currentPpid")
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    private fun showRetryOptions() {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Gagal Memuat Data")
            .setMessage("Tidak dapat memuat data profil. Coba lagi?")
            .setPositiveButton("Coba Lagi") { _, _ ->
                viewModel.refreshData()
            }
            .setNegativeButton("Tutup") { _, _ ->
                finish()
            }
            .setCancelable(false)
            .show()
    }

    override fun onResume() {
        super.onResume()
        LoggingUtils.logDebug(TAG, "DetailLoketActivity resumed for PPID: $currentPpid")
    }

    override fun onDestroy() {
        super.onDestroy()
        LoggingUtils.logDebug(TAG, "DetailLoketActivity destroyed")
    }
}


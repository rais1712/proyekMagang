// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/TransactionLogActivity.kt - FIXED FINAL
package com.proyek.maganggsp.presentation.detail_loket

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ActivityDetailLoketBinding
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.domain.model.TransactionLog
import com.proyek.maganggsp.presentation.profile.UpdateProfileActivity
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

    // Activity result launcher untuk update profile
    private val updateProfileLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            Log.d(TAG, "✅ Profile updated successfully, refreshing data")
            viewModel.refreshData()
            AppUtils.showSuccess(this, "Profil berhasil diupdate")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.i(TAG, "🔄 FIXED TransactionLogActivity - Complete dengan profile info")

        binding = ActivityDetailLoketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupToolbar()
        setupRecyclerView()
        setupActionListeners()
        observeStates()
    }

    private fun setupUI() {
        // Update labels untuk Transaction Log context (Bahasa Indonesia)
        binding.tvMutasiTitle.text = "Log Transaksi"

        // Hide unused buttons dan show yang dibutuhkan
        binding.btnBlock.isVisible = false
        binding.btnUnblock.isVisible = false
        binding.btnClearFlags.isVisible = false
        binding.btnDiblokir.isVisible = false

        Log.d(TAG, "🎨 UI setup completed dengan Indonesian labels")
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
            Log.d(TAG, "📱 Back navigation triggered")
        }

        binding.toolbar.title = "Detail Transaction Log"
    }

    private fun setupRecyclerView() {
        transactionLogAdapter = TransactionLogAdapter()
        binding.rvMutations.apply {
            adapter = transactionLogAdapter
            layoutManager = LinearLayoutManager(this@TransactionLogActivity)
            isNestedScrollingEnabled = false
        }

        // Setup click listener untuk transaction items
        transactionLogAdapter.setOnItemClickListener { transactionLog ->
            showTransactionDetails(transactionLog)
        }

        Log.d(TAG, "✅ TransactionLog RecyclerView setup completed")
    }

    private fun setupActionListeners() {
        // UPDATE: Repurpose Block button sebagai Update Profile button
        binding.btnBlock.apply {
            text = "Update Profile"
            setBackgroundColor(ContextCompat.getColor(this@TransactionLogActivity, R.color.blue_accent_action))
            isVisible = true

            setOnClickListener {
                Log.d(TAG, "🔄 Update profile button clicked")
                navigateToUpdateProfile()
            }
        }
    }

    private fun navigateToUpdateProfile() {
        val currentPpid = viewModel.getCurrentPpid()

        if (currentPpid.isNullOrBlank()) {
            AppUtils.showError(this, "PPID tidak tersedia")
            return
        }

        val intent = Intent(this, UpdateProfileActivity::class.java).apply {
            putExtra(NavigationConstants.ARG_PPID, currentPpid)
        }

        updateProfileLauncher.launch(intent)
        Log.d(TAG, "🚀 Navigating to UpdateProfileActivity dengan ppid: $currentPpid")
    }

    private fun showTransactionDetails(transactionLog: TransactionLog) {
        val details = """
        Detail Transaksi:
        
        Referensi: ${transactionLog.tldRefnum}
        PAN: ${transactionLog.tldPan}
        ID Pelanggan: ${transactionLog.tldIdpel}
        Nominal: ${transactionLog.getFormattedAmount()}
        Saldo: ${transactionLog.getFormattedBalance()}
        Tanggal: ${transactionLog.getFormattedDate()}
        PPID: ${transactionLog.tldPpid}
        """.trimIndent()

        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("Detail Transaksi")
            .setMessage(details)
            .setPositiveButton("OK", null)
            .show()

        Log.d(TAG, "📋 Showing transaction details for: ${transactionLog.tldRefnum}")
    }

    private fun observeStates() {
        // Observe profile info state
        lifecycleScope.launch {
            viewModel.profileState.collectLatest { resource ->
                handleProfileInfoState(resource)
            }
        }

        // Observe transaction logs state
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

    private fun handleProfileInfoState(resource: Resource<Receipt>) {
        when (resource) {
            is Resource.Success -> {
                val receipt = resource.data
                if (receipt != null) {
                    updateProfileInfoUI(receipt)
                    Log.d(TAG, "✅ Profile info loaded: ${receipt.refNumber}")
                }
            }
            is Resource.Error -> {
                Log.e(TAG, "❌ Profile info error: ${resource.message}")
                updateProfileInfoPlaceholder()
            }
            is Resource.Loading -> {
                Log.d(TAG, "⏳ Loading profile info...")
                showProfileInfoShimmer(true)
            }
            else -> Unit
        }
    }

    private fun updateProfileInfoUI(receipt: Receipt) {
        showProfileInfoShimmer(false)

        binding.apply {
            // Update card dengan Receipt info
            tvLoketId.text = "Ref: ${receipt.refNumber}"
            tvLoketName.text = "ID: ${receipt.idPelanggan}"
            tvPhoneValue.text = "Nominal: ${AppUtils.formatCurrency(receipt.amount)}"
            tvEmailValue.text = "Logged: ${AppUtils.formatDate(receipt.logged)}"

            // Set default status chip
            chipStatus.text = "Aktif"
            chipStatus.setChipBackgroundColor(
                ContextCompat.getColorStateList(this@TransactionLogActivity, R.color.chip_normal_background)
            )
        }

        Log.d(TAG, "🔄 Profile info UI updated dengan receipt data")
    }

    private fun updateProfileInfoPlaceholder() {
        showProfileInfoShimmer(false)

        binding.apply {
            tvLoketId.text = "PPID: ${viewModel.getCurrentPpid() ?: "N/A"}"
            tvLoketName.text = "Profile Data"
            tvPhoneValue.text = "Placeholder untuk testing"
            tvEmailValue.text = "Data tidak tersedia"

            chipStatus.text = "Testing"
            chipStatus.setChipBackgroundColor(
                ContextCompat.getColorStateList(this@TransactionLogActivity, R.color.yellow_secondary_accent)
            )
        }

        Log.d(TAG, "📋 Profile info placeholder displayed")
    }

    private fun showProfileInfoShimmer(show: Boolean) {
        binding.shimmerCardInfo.isVisible = show
        binding.cardLoketInfo.isVisible = !show

        if (show) {
            binding.shimmerCardInfo.startShimmer()
        } else {
            binding.shimmerCardInfo.stopShimmer()
        }
    }

    private fun handleTransactionLogsState(resource: Resource<List<TransactionLog>>) {
        // Gunakan AppUtils untuk consistent state management
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

                transactionLogAdapter.updateData(data)

                // Apply contextual empty state (Indonesian)
                AppUtils.applyEmptyState(
                    textView = binding.tvMutationsError,
                    context = "transactions",
                    itemCount = data.size
                )
            }
            is Resource.Error -> {
                Log.e(TAG, "❌ Transaction logs error: ${resource.message}")
                binding.tvMutationsError.text = "Gagal memuat log transaksi.\nTarik ke bawah untuk refresh."
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
        binding.btnBlock.isEnabled = resource !is Resource.Loading

        when (resource) {
            is Resource.Error -> {
                AppUtils.showError(this, resource.exception)
                viewModel.onActionConsumed()
                Log.e(TAG, "❌ Action error: ${resource.message}")
            }
            is Resource.Success -> {
                AppUtils.showSuccess(this, "Aksi berhasil")
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
            is TransactionLogViewModel.UiEvent.NavigateToUpdateProfile -> {
                navigateToUpdateProfile()
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "🧹 TransactionLogActivity destroyed")
    }
}
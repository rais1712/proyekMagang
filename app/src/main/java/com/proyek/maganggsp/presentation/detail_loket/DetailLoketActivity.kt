// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/DetailLoketActivity.kt
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
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.applyToLoadingViews
import com.proyek.maganggsp.util.FeatureFlags
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailLoketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailLoketBinding
    private val viewModel: DetailLoketViewModel by viewModels()
    private lateinit var mutasiAdapter: MutasiAdapter

    companion object {
        private const val TAG = "DetailLoketActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 🚩 FEATURE FLAGS: Log current configuration
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.i(TAG, "🚩 DetailLoketActivity created with feature flags:")
            Log.d(TAG, "Detail view enabled: ${FeatureFlags.ENABLE_LOKET_DETAIL_VIEW}")
            Log.d(TAG, "Mutation history enabled: ${FeatureFlags.ENABLE_MUTATION_HISTORY}")
            Log.d(TAG, "Actions enabled: ${FeatureFlags.ENABLE_LOKET_ACTIONS}")
            Log.d(TAG, "Simplified mode: ${FeatureFlags.isDetailScreenSimplified()}")
        }

        binding = ActivityDetailLoketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupUI()
        setupToolbar()
        setupRecyclerView()
        setupActionListeners()
        observeStates()
    }

    private fun setupUI() {
        // 🚩 FEATURE FLAGS: Conditional UI setup based on enabled features

        // Mutation History Section
        if (FeatureFlags.ENABLE_MUTATION_HISTORY) {
            binding.mutationsShimmerLayout.isVisible = true
            binding.rvMutations.isVisible = true

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Mutation history section enabled")
            }
        } else {
            binding.mutationsShimmerLayout.isVisible = false
            binding.rvMutations.isVisible = false
            binding.tvMutationsError.isVisible = true
            binding.tvMutationsError.text = "Fitur riwayat mutasi sedang dikembangkan"

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Mutation history section disabled")
            }
        }

        // Action Buttons
        if (FeatureFlags.ENABLE_LOKET_ACTIONS) {
            binding.btnBlock.isVisible = true
            binding.btnUnblock.isVisible = true
            binding.btnClearFlags.isVisible = FeatureFlags.ENABLE_FLAG_MANAGEMENT

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Action buttons enabled")
            }
        } else {
            binding.btnBlock.isVisible = false
            binding.btnUnblock.isVisible = false
            binding.btnClearFlags.isVisible = false

            // Show info message
            Toast.makeText(this, "Mode hanya-baca: Fitur aksi sedang dikembangkan", Toast.LENGTH_LONG).show()

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Action buttons disabled - read-only mode")
            }
        }

        // Flag Management
        if (!FeatureFlags.ENABLE_FLAG_MANAGEMENT) {
            binding.btnClearFlags.isVisible = false

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Flag management disabled")
            }
        }
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Back navigation triggered")
            }
        }
    }

    private fun setupRecyclerView() {
        // 🚩 FEATURE FLAGS: Only setup RecyclerView if mutation history is enabled
        if (FeatureFlags.ENABLE_MUTATION_HISTORY) {
            mutasiAdapter = MutasiAdapter()
            binding.rvMutations.apply {
                adapter = mutasiAdapter
                layoutManager = LinearLayoutManager(this@DetailLoketActivity)
                isNestedScrollingEnabled = false
            }

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Mutation RecyclerView setup completed")
            }
        } else {
            // Create empty adapter to prevent crashes
            mutasiAdapter = MutasiAdapter()

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Empty adapter created - mutation history disabled")
            }
        }
    }

    private fun setupActionListeners() {
        // 🚩 FEATURE FLAGS: Only setup listeners if actions are enabled
        if (FeatureFlags.ENABLE_LOKET_ACTIONS) {
            binding.btnBlock.setOnClickListener {
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.d(TAG, "🚩 Block button clicked")
                }
                viewModel.blockLoket()
            }

            binding.btnUnblock.setOnClickListener {
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.d(TAG, "🚩 Unblock button clicked")
                }
                viewModel.unblockLoket()
            }

            if (FeatureFlags.ENABLE_FLAG_MANAGEMENT) {
                binding.btnClearFlags.setOnClickListener {
                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.d(TAG, "🚩 Clear flags button clicked")
                    }
                    viewModel.clearAllFlags()
                }
            }
        } else {
            // Disable button interactions
            binding.btnBlock.setOnClickListener {
                showFeatureDisabledMessage("Fitur blokir")
            }

            binding.btnUnblock.setOnClickListener {
                showFeatureDisabledMessage("Fitur buka blokir")
            }

            binding.btnClearFlags.setOnClickListener {
                showFeatureDisabledMessage("Fitur hapus penanda")
            }

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Action listeners disabled - showing info messages instead")
            }
        }
    }

    private fun showFeatureDisabledMessage(featureName: String) {
        Toast.makeText(this, "$featureName sedang dikembangkan", Toast.LENGTH_SHORT).show()

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.w(TAG, "🚩 Feature disabled message shown: $featureName")
        }
    }

    private fun observeStates() {
        // Observer untuk detail loket
        lifecycleScope.launch {
            viewModel.loketDetailsState.collectLatest { resource ->
                handleLoketDetailsState(resource)
            }
        }

        // Observer untuk mutations (hanya jika enabled)
        if (FeatureFlags.ENABLE_MUTATION_HISTORY) {
            lifecycleScope.launch {
                viewModel.mutationsState.collectLatest { resource ->
                    handleMutationsState(resource)
                }
            }
        } else {
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Skipping mutation state observer - feature disabled")
            }
        }

        // Observer untuk action state (hanya jika actions enabled)
        if (FeatureFlags.ENABLE_LOKET_ACTIONS) {
            lifecycleScope.launch {
                viewModel.actionState.collectLatest { resource ->
                    handleActionState(resource)
                }
            }
        }

        // Observer untuk events
        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest { event ->
                handleViewModelEvents(event)
            }
        }
    }

    private fun handleLoketDetailsState(resource: Resource<Loket>) {
        // 🚩 FEATURE FLAGS: Conditional loading management
        if (FeatureFlags.ENABLE_SHIMMER_LOADING) {
            resource.applyToLoadingViews(
                shimmerView = binding.shimmerCardInfo,
                contentView = binding.cardLoketInfo
            )
        } else {
            // Simple loading
            binding.shimmerCardInfo.isVisible = resource is Resource.Loading
            binding.cardLoketInfo.isVisible = resource is Resource.Success

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Using simple loading - shimmer disabled")
            }
        }

        when (resource) {
            is Resource.Success -> {
                updateLoketInfo(resource.data)

                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.d(TAG, "🚩 Loket details loaded: ${resource.data.noLoket}")
                }
            }
            is Resource.Error -> {
                val errorMessage = if (FeatureFlags.ENABLE_DETAILED_ERROR_MESSAGES) {
                    resource.exception.message
                } else {
                    "Gagal memuat detail loket"
                }

                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()

                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.e(TAG, "🚩 Loket details error: ${resource.exception.message}")
                }
            }
            else -> Unit
        }
    }

    private fun handleMutationsState(resource: Resource<List<com.proyek.maganggsp.domain.model.Mutasi>>) {
        if (!FeatureFlags.ENABLE_MUTATION_HISTORY) return

        // 🚩 FEATURE FLAGS: Conditional loading management
        if (FeatureFlags.ENABLE_SHIMMER_LOADING) {
            resource.applyToLoadingViews(
                shimmerView = binding.mutationsShimmerLayout,
                contentView = binding.rvMutations,
                emptyView = binding.tvMutationsError
            )
        } else {
            binding.mutationsShimmerLayout.isVisible = resource is Resource.Loading
            binding.rvMutations.isVisible = resource is Resource.Success && (resource.data?.isNotEmpty() == true)
            binding.tvMutationsError.isVisible = resource !is Resource.Loading && (resource !is Resource.Success || resource.data?.isEmpty() == true)
        }

        when (resource) {
            is Resource.Success -> {
                val data = resource.data ?: emptyList()

                if (data.isEmpty()) {
                    binding.tvMutationsError.text = "Tidak ada riwayat mutasi."
                } else {
                    mutasiAdapter.differ.submitList(data)
                }

                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.d(TAG, "🚩 Mutations loaded: ${data.size} items")
                }
            }
            is Resource.Error -> {
                val errorMessage = if (FeatureFlags.ENABLE_DETAILED_ERROR_MESSAGES) {
                    getString(R.string.error_load_mutations)
                } else {
                    "Gagal memuat data"
                }
                binding.tvMutationsError.text = errorMessage

                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.e(TAG, "🚩 Mutations error: ${resource.exception.message}")
                }
            }
            is Resource.Empty -> {
                binding.tvMutationsError.text = "Tidak ada riwayat mutasi."
            }
            else -> Unit
        }
    }

    private fun handleActionState(resource: Resource<Unit>) {
        if (!FeatureFlags.ENABLE_LOKET_ACTIONS) return

        binding.mainProgressBar.isVisible = resource is Resource.Loading
        setButtonsEnabled(resource !is Resource.Loading)

        when (resource) {
            is Resource.Error -> {
                val errorMessage = if (FeatureFlags.ENABLE_DETAILED_ERROR_MESSAGES) {
                    resource.exception.message
                } else {
                    "Aksi gagal"
                }
                Toast.makeText(this, errorMessage, Toast.LENGTH_LONG).show()
                viewModel.onActionConsumed()

                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.e(TAG, "🚩 Action error: ${resource.exception.message}")
                }
            }
            is Resource.Success -> {
                viewModel.onActionConsumed()

                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.d(TAG, "🚩 Action completed successfully")
                }
            }
            else -> Unit
        }
    }

    private fun handleViewModelEvents(event: DetailLoketViewModel.UiEvent) {
        when (event) {
            is DetailLoketViewModel.UiEvent.ShowToast -> {
                Toast.makeText(this, event.message, Toast.LENGTH_SHORT).show()

                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.d(TAG, "🚩 Toast event: ${event.message}")
                }
            }
        }
    }

    private fun setButtonsEnabled(isEnabled: Boolean) {
        if (!FeatureFlags.ENABLE_LOKET_ACTIONS) return

        binding.btnBlock.isEnabled = isEnabled
        binding.btnUnblock.isEnabled = isEnabled

        if (FeatureFlags.ENABLE_FLAG_MANAGEMENT) {
            binding.btnClearFlags.isEnabled = isEnabled
        }

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 Buttons enabled state: $isEnabled")
        }
    }

    private fun updateLoketInfo(loket: Loket) {
        if (!FeatureFlags.ENABLE_LOKET_DETAIL_VIEW) {
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Detail view disabled - skipping UI update")
            }
            return
        }

        binding.tvLoketId.text = loket.noLoket
        binding.tvLoketName.text = loket.namaLoket
        binding.tvPhoneValue.text = loket.nomorTelepon
        binding.tvEmailValue.text = loket.email

        // Status handling with feature flag consideration
        when (loket.status.uppercase()) {
            "DIBLOKIR" -> {
                binding.chipStatus.text = getString(R.string.status_diblokir)
                binding.chipStatus.setChipBackgroundColor(
                    ContextCompat.getColorStateList(this, R.color.chip_blocked_background)
                )

                // 🚩 FEATURE FLAGS: Conditional button visibility
                if (FeatureFlags.ENABLE_LOKET_ACTIONS) {
                    binding.btnBlock.isVisible = false
                    binding.btnUnblock.isVisible = true
                }
            }
            "DIPANTAU" -> {
                binding.chipStatus.text = getString(R.string.status_ditandai)
                binding.chipStatus.setChipBackgroundColor(
                    ContextCompat.getColorStateList(this, R.color.chip_flagged_background)
                )

                if (FeatureFlags.ENABLE_LOKET_ACTIONS) {
                    binding.btnBlock.isVisible = true
                    binding.btnUnblock.isVisible = false
                }
            }
            else -> { // AKTIF / NORMAL
                binding.chipStatus.text = getString(R.string.status_normal)
                binding.chipStatus.setChipBackgroundColor(
                    ContextCompat.getColorStateList(this, R.color.chip_normal_background)
                )

                if (FeatureFlags.ENABLE_LOKET_ACTIONS) {
                    binding.btnBlock.isVisible = true
                    binding.btnUnblock.isVisible = false
                }
            }
        }

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 Loket info updated - Status: ${loket.status}")
        }
    }

    override fun onDestroy() {
        super.onDestroy()

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 DetailLoketActivity destroyed")
        }
    }
}
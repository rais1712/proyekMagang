package com.proyek.maganggsp.presentation.detailloket

import android.os.Bundle
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class DetailLoketActivity : AppCompatActivity() {

    private lateinit var binding: ActivityDetailLoketBinding
    private val viewModel: DetailLoketViewModel by viewModels()
    private lateinit var mutasiAdapter: MutasiAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDetailLoketBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupToolbar()
        setupRecyclerView()
        setupActionListeners()
        observeStates()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        mutasiAdapter = MutasiAdapter()
        binding.rvMutations.apply {
            adapter = mutasiAdapter
            layoutManager = LinearLayoutManager(this@DetailLoketActivity)
            isNestedScrollingEnabled = false
        }
    }

    private fun setupActionListeners() {
        binding.btnBlock.setOnClickListener { viewModel.blockLoket() }
        binding.btnUnblock.setOnClickListener { viewModel.unblockLoket() }
        binding.btnClearFlags.setOnClickListener { viewModel.clearAllFlags() }
    }

    private fun observeStates() {
        // Observer untuk detail loket dengan standardized loading management
        lifecycleScope.launch {
            viewModel.loketDetailsState.collectLatest { resource ->
                // ENHANCED: Menggunakan unified loading state management
                resource.applyToLoadingViews(
                    shimmerView = binding.shimmerCardInfo,
                    contentView = binding.cardLoketInfo
                )

                when(resource) {
                    is Resource.Success -> {
                        updateLoketInfo(resource.data)
                    }
                    is Resource.Error -> {
                        Toast.makeText(this@DetailLoketActivity, resource.exception.message, Toast.LENGTH_LONG).show()
                    }
                    else -> Unit // Loading state dihandle oleh extension
                }
            }
        }

        // Observer untuk daftar mutasi dengan standardized loading management
        lifecycleScope.launch {
            viewModel.mutationsState.collectLatest { resource ->
                // ENHANCED: Unified loading state management untuk mutations
                resource.applyToLoadingViews(
                    shimmerView = binding.mutationsShimmerLayout,
                    contentView = binding.rvMutations,
                    emptyView = binding.tvMutationsError
                )

                when(resource) {
                    is Resource.Success -> {
                        if (resource.data.isEmpty()) {
                            binding.tvMutationsError.text = "Tidak ada riwayat mutasi."
                        } else {
                            mutasiAdapter.differ.submitList(resource.data)
                        }
                    }
                    is Resource.Error -> {
                        binding.tvMutationsError.text = getString(R.string.error_load_mutations)
                    }
                    is Resource.Empty -> {
                        binding.tvMutationsError.text = "Tidak ada riwayat mutasi."
                    }
                    else -> Unit // Loading state dihandle oleh extension
                }
            }
        }

        // Observer untuk umpan balik aksi
        lifecycleScope.launch {
            viewModel.actionState.collectLatest { resource ->
                binding.mainProgressBar.isVisible = resource is Resource.Loading
                setButtonsEnabled(resource !is Resource.Loading)

                if (resource is Resource.Error) {
                    Toast.makeText(this@DetailLoketActivity, resource.exception.message, Toast.LENGTH_LONG).show()
                    viewModel.onActionConsumed()
                } else if (resource is Resource.Success) {
                    viewModel.onActionConsumed()
                }
            }
        }

        // Observer untuk event Toast
        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest { event ->
                if (event is DetailLoketViewModel.UiEvent.ShowToast) {
                    Toast.makeText(this@DetailLoketActivity, event.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setButtonsEnabled(isEnabled: Boolean) {
        binding.btnBlock.isEnabled = isEnabled
        binding.btnUnblock.isEnabled = isEnabled
        binding.btnClearFlags.isEnabled = isEnabled
    }

    private fun updateLoketInfo(loket: Loket) {
        binding.tvLoketId.text = loket.noLoket
        binding.tvLoketName.text = loket.namaLoket
        binding.tvPhoneValue.text = loket.nomorTelepon
        binding.tvEmailValue.text = loket.email

        when (loket.status.uppercase()) {
            "DIBLOKIR" -> {
                binding.chipStatus.text = getString(R.string.status_diblokir)
                binding.chipStatus.setChipBackgroundColor(ContextCompat.getColorStateList(this, R.color.chip_blocked_background))
                binding.btnBlock.isVisible = false
                binding.btnUnblock.isVisible = true
                binding.btnDiblokir.isVisible = false
            }
            "DIPANTAU" -> {
                binding.chipStatus.text = getString(R.string.status_ditandai)
                binding.chipStatus.setChipBackgroundColor(ContextCompat.getColorStateList(this, R.color.chip_flagged_background))
                binding.btnBlock.isVisible = true
                binding.btnUnblock.isVisible = false
                binding.btnDiblokir.isVisible = false
            }
            else -> { // AKTIF / NORMAL
                binding.chipStatus.text = getString(R.string.status_normal)
                binding.chipStatus.setChipBackgroundColor(ContextCompat.getColorStateList(this, R.color.chip_normal_background))
                binding.btnBlock.isVisible = true
                binding.btnUnblock.isVisible = false
                binding.btnDiblokir.isVisible = false
            }
        }
    }
}
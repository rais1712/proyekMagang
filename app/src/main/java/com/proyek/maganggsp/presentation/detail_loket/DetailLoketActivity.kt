package com.proyek.maganggsp.presentation.detail_loket

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ActivityDetailLoketBinding
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.util.Formatters
import com.proyek.maganggsp.util.Resource
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
        observeLoketDetails()
        observeMutations()
        observeActionState()
        observeUiEvents()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun setupRecyclerView() {
        mutasiAdapter = MutasiAdapter()
        binding.rvMutations.adapter = mutasiAdapter
        mutasiAdapter.setOnLongClickListener { mutasi ->
            viewModel.flagMutation(mutasi.id)
        }
    }

    private fun setupActionListeners() {
        binding.btnBlock.setOnClickListener {
            showConfirmationDialog(
                title = "Konfirmasi Blokir",
                message = "Apakah Anda yakin ingin memblokir loket ini?",
                positiveButtonText = "Ya, Blokir"
            ) {
                viewModel.blockLoket()
            }
        }

        binding.btnUnblock.setOnClickListener {
            showConfirmationDialog(
                title = "Konfirmasi Buka Blokir",
                message = "Apakah Anda yakin ingin membuka blokir loket ini?",
                positiveButtonText = "Ya, Buka Blokir"
            ) {
                viewModel.unblockLoket()
            }
        }

        binding.btnClearFlags.setOnClickListener {
            // Aksi ini tidak memerlukan dialog konfirmasi
            viewModel.clearAllFlags()
        }
    }

    private fun observeLoketDetails() {
        lifecycleScope.launch {
            viewModel.loketDetailsState.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.shimmerCardInfo.startShimmer()
                        binding.shimmerCardInfo.isVisible = true
                        binding.cardLoketInfo.isVisible = false
                    }
                    is Resource.Success -> {
                        binding.shimmerCardInfo.stopShimmer()
                        binding.shimmerCardInfo.isVisible = false
                        binding.cardLoketInfo.isVisible = true

                        resource.data?.let { loket ->
                            updateLoketInfoUI(loket)
                        }
                    }
                    is Resource.Error -> {
                        binding.shimmerCardInfo.stopShimmer()
                        binding.shimmerCardInfo.isVisible = false
                        binding.cardLoketInfo.isVisible = false
                        // TODO: Handle Error State for Loket Details (misal: tampilkan halaman error)
                        Toast.makeText(this@DetailLoketActivity, resource.message, Toast.LENGTH_LONG).show()
                    }
                    is Resource.Empty -> {
                        binding.shimmerCardInfo.stopShimmer()
                        binding.shimmerCardInfo.isVisible = false
                        binding.cardLoketInfo.isVisible = false
                        // Handle empty state for loket details
                    }
                }
            }
        }
    }

    private fun observeMutations() {
        lifecycleScope.launch {
            viewModel.mutationsState.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.mutationsShimmerLayout.startShimmer()
                        binding.mutationsShimmerLayout.isVisible = true
                        binding.rvMutations.isVisible = false
                        binding.tvMutationsError.isVisible = false
                        // Hide empty view jika ada
                        // binding.tvMutationsEmpty.isVisible = false
                    }
                    is Resource.Success -> {
                        binding.mutationsShimmerLayout.stopShimmer()
                        binding.mutationsShimmerLayout.isVisible = false
                        binding.rvMutations.isVisible = true
                        binding.tvMutationsError.isVisible = false

                        mutasiAdapter.differ.submitList(resource.data)
                    }
                    is Resource.Error -> {
                        binding.mutationsShimmerLayout.stopShimmer()
                        binding.mutationsShimmerLayout.isVisible = false
                        binding.rvMutations.isVisible = false
                        binding.tvMutationsError.isVisible = true
                        binding.tvMutationsError.text = resource.message
                    }
                    is Resource.Empty -> {
                        binding.mutationsShimmerLayout.stopShimmer()
                        binding.mutationsShimmerLayout.isVisible = false
                        binding.rvMutations.isVisible = false
                        // Jika Anda memiliki TextView untuk empty state, uncomment baris di bawah
                        // binding.tvMutationsEmpty.isVisible = true
                        // binding.tvMutationsEmpty.text = "Tidak ada data mutasi"
                    }
                }
            }
        }
    }

    private fun observeActionState() {
        lifecycleScope.launch {
            viewModel.actionState.collectLatest { resource ->
                val isLoading = resource is Resource.Loading
                binding.mainProgressBar.isVisible = isLoading

                // Nonaktifkan semua tombol aksi saat loading
                binding.btnBlock.isEnabled = !isLoading
                binding.btnUnblock.isEnabled = !isLoading

                // Atur ulang status tombol hapus tanda berdasarkan status loket saat ini
                val currentStatus = viewModel.loketDetailsState.value.data?.status?.lowercase()
                binding.btnClearFlags.isEnabled = !isLoading && currentStatus == "dipantau"
            }
        }
    }

    private fun observeUiEvents() {
        lifecycleScope.launch {
            viewModel.eventFlow.collectLatest { event ->
                when (event) {
                    is DetailLoketViewModel.UiEvent.ShowToast -> {
                        Toast.makeText(this@DetailLoketActivity, event.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private fun updateLoketInfoUI(loket: Loket) {
        // Mengisi semua data loket
        binding.tvLoketId.text = "#${loket.id}"
        binding.tvLoketName.text = loket.name
        binding.tvPhoneValue.text = loket.phoneNumber
        binding.tvEmailValue.text = "email.placeholder@example.com" // Asumsi ada email
        // Anda perlu menambahkan field alamat dan saldo di layout untuk bisa menampilkannya
        // binding.tvAddressValue.text = loket.address
        // binding.tvBalanceValue.text = Formatters.toRupiah(loket.balance)


        // Mengatur tampilan UI berdasarkan status loket
        when (loket.status.lowercase()) {
            "diblokir" -> {
                binding.chipStatus.text = "Diblokir"
                binding.chipStatus.setChipBackgroundColorResource(R.color.red_danger)
                binding.layoutLoketInfo.setBackgroundResource(R.drawable.bg_card_info_diblokir)
                binding.btnBlock.isVisible = false
                binding.btnDiblokir.isVisible = true // Menampilkan tombol status "Diblokir"
                binding.btnUnblock.isVisible = true
                binding.btnClearFlags.isVisible = false
            }
            "dipantau" -> {
                binding.chipStatus.text = "Dipantau"
                binding.chipStatus.setChipBackgroundColorResource(R.color.yellow_secondary_accent)
                binding.layoutLoketInfo.setBackgroundResource(R.drawable.bg_card_info_ditandai)
                binding.btnBlock.isVisible = true
                binding.btnDiblokir.isVisible = false
                binding.btnUnblock.isVisible = false
                binding.btnClearFlags.isVisible = true
                binding.btnClearFlags.isEnabled = true
            }
            else -> { // Aktif atau Normal
                binding.chipStatus.text = "Normal"
                binding.chipStatus.setChipBackgroundColorResource(R.color.chip_normal_background)
                binding.layoutLoketInfo.setBackgroundResource(R.drawable.bg_card_info_normal)
                binding.btnBlock.isVisible = true
                binding.btnDiblokir.isVisible = false
                binding.btnUnblock.isVisible = false
                binding.btnClearFlags.isVisible = true
                // Tombolnya ada tapi dinonaktifkan
                binding.btnClearFlags.isEnabled = false
            }
        }
    }

    private fun showConfirmationDialog(
        title: String,
        message: String,
        positiveButtonText: String,
        onConfirm: () -> Unit
    ) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setNegativeButton("Batal", null)
            .setPositiveButton(positiveButtonText) { _, _ ->
                onConfirm()
            }
            .show()
    }
}
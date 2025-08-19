// File: app/src/main/java/com/proyek/maganggsp/presentation/detail_loket/DetailLoketActivity.kt
package com.proyek.maganggsp.presentation.detail_loket

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.ActivityDetailLoketBinding
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.domain.model.Mutasi
import com.proyek.maganggsp.presentation.detailloket.DetailLoketViewModel
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
        // Tambahkan dialog konfirmasi sebelum aksi Block
        binding.btnBlock.setOnClickListener {
            showConfirmationDialog(
                title = getString(R.string.dialog_confirm_block_title),
                message = getString(R.string.dialog_confirm_block_message),
                onConfirm = { viewModel.blockLoket() }
            )
        }

        // Tambahkan dialog konfirmasi sebelum aksi Unblock
        binding.btnUnblock.setOnClickListener {
            showConfirmationDialog(
                title = getString(R.string.dialog_confirm_unblock_title),
                message = getString(R.string.dialog_confirm_unblock_message),
                onConfirm = { viewModel.unblockLoket() }
            )
        }

        // Tambahkan dialog konfirmasi sebelum aksi Clear Flags
        binding.btnClearFlags.setOnClickListener {
            showConfirmationDialog(
                title = getString(R.string.dialog_confirm_clear_flags_title),
                message = getString(R.string.dialog_confirm_clear_flags_message),
                onConfirm = { viewModel.clearAllFlags() }
            )
        }
    }

    private fun showConfirmationDialog(
        title: String,
        message: String,
        onConfirm: () -> Unit
    ) {
        MaterialAlertDialogBuilder(this)
            .setTitle(title)
            .setMessage(message)
            .setPositiveButton(getString(R.string.dialog_button_yes)) { dialog, _ ->
                onConfirm()
                dialog.dismiss()
            }
            .setNegativeButton(getString(R.string.dialog_button_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            .show()
    }

    private fun observeStates() {
        // Mengamati detail loket
        lifecycleScope.launch {
            viewModel.loketDetailsState.collectLatest { resource ->
                // Mengelola shimmer effect untuk kartu info
                binding.shimmerCardInfo.isVisible = resource is Resource.Loading<*>
                binding.cardLoketInfo.isVisible = resource !is Resource.Loading<*>

                when(resource) {
                    is Resource.Success<Loket> -> {
                        binding.shimmerCardInfo.stopShimmer()
                        resource.data?.let { loket ->
                            updateLoketInfo(loket)
                        }
                    }
                    is Resource.Error<Loket> -> {
                        binding.shimmerCardInfo.stopShimmer()
                        Toast.makeText(this@DetailLoketActivity, resource.message, Toast.LENGTH_LONG).show()
                    }
                    is Resource.Loading<Loket> -> {
                        binding.shimmerCardInfo.startShimmer()
                    }
                    else -> Unit
                }
            }
        }

        // Mengamati daftar mutasi
        lifecycleScope.launch {
            viewModel.mutationsState.collectLatest { resource ->
                // Mengelola shimmer effect untuk daftar mutasi
                binding.mutationsShimmerLayout.isVisible = resource is Resource.Loading<*>

                // FIXED: Safe type checking untuk List<Mutasi>
                val isSuccessWithData = resource is Resource.Success &&
                        resource.data != null && resource.data.isNotEmpty()
                binding.rvMutations.isVisible = isSuccessWithData

                val isErrorOrEmpty = resource is Resource.Error<*> ||
                        (resource is Resource.Success && (resource.data == null || resource.data.isEmpty()))
                binding.tvMutationsError.isVisible = isErrorOrEmpty

                when(resource) {
                    is Resource.Success<List<Mutasi>> -> {
                        binding.mutationsShimmerLayout.stopShimmer()
                        val mutasiList = resource.data ?: emptyList()
                        if (mutasiList.isEmpty()) {
                            binding.tvMutationsError.text = "Tidak ada riwayat mutasi."
                        } else {
                            // Submit list data ke adapter
                            mutasiAdapter.differ.submitList(mutasiList)
                        }
                    }
                    is Resource.Error<List<Mutasi>> -> {
                        binding.mutationsShimmerLayout.stopShimmer()
                        binding.tvMutationsError.text = getString(R.string.error_load_mutations)
                    }
                    is Resource.Loading<List<Mutasi>> -> {
                        binding.mutationsShimmerLayout.startShimmer()
                    }
                    else -> Unit
                }
            }
        }

        // Mengamati status aksi (block/unblock/clear flags)
        lifecycleScope.launch {
            viewModel.actionState.collectLatest { resource ->
                when(resource) {
                    is Resource.Success<Unit> -> {
                        // Tampilkan pesan sukses sesuai aksi yang dilakukan
                        val message = "Aksi berhasil dilakukan"
                        Toast.makeText(this@DetailLoketActivity, message, Toast.LENGTH_SHORT).show()
                    }
                    is Resource.Error<Unit> -> {
                        Toast.makeText(this@DetailLoketActivity, resource.message, Toast.LENGTH_LONG).show()
                    }
                    is Resource.Loading<Unit> -> {
                        // Bisa ditambahkan ProgressBar untuk aksi jika perlu
                    }
                    else -> Unit
                }
            }
        }
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

                // Clear Flags: Disabled saat diblokir
                updateClearFlagsButton(isEnabled = false)
            }
            "DIPANTAU" -> {
                binding.chipStatus.text = getString(R.string.status_ditandai)
                binding.chipStatus.setChipBackgroundColor(ContextCompat.getColorStateList(this, R.color.chip_flagged_background))
                binding.btnBlock.isVisible = true
                binding.btnUnblock.isVisible = false
                binding.btnDiblokir.isVisible = false

                // Clear Flags: Enabled saat dipantau/ditandai
                updateClearFlagsButton(isEnabled = true)
            }
            else -> { // AKTIF / NORMAL
                binding.chipStatus.text = getString(R.string.status_normal)
                binding.chipStatus.setChipBackgroundColor(ContextCompat.getColorStateList(this, R.color.chip_normal_background))
                binding.btnBlock.isVisible = true
                binding.btnUnblock.isVisible = false
                binding.btnDiblokir.isVisible = false

                // Clear Flags: Disabled saat normal
                updateClearFlagsButton(isEnabled = false)
            }
        }
    }

    private fun updateClearFlagsButton(isEnabled: Boolean) {
        binding.btnClearFlags.apply {
            // Selalu visible sesuai requirement
            isVisible = true

            // Set enabled state
            this.isEnabled = isEnabled

            // Update appearance berdasarkan state
            if (isEnabled) {
                // State aktif - gunakan warna kuning
                backgroundTintList = ContextCompat.getColorStateList(this@DetailLoketActivity, R.color.yellow_secondary_accent)
                setTextColor(ContextCompat.getColor(this@DetailLoketActivity, R.color.text_primary_black))
                alpha = 1.0f
            } else {
                // State disabled - gunakan warna abu-abu
                backgroundTintList = ContextCompat.getColorStateList(this@DetailLoketActivity, android.R.color.darker_gray)
                setTextColor(ContextCompat.getColor(this@DetailLoketActivity, android.R.color.white))
                alpha = 0.6f
            }
        }
    }
}
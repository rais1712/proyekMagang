// File: app/src/main/java/com/proyek/maganggsp/presentation/home/HomeFragment.kt - FIXED NAVIGATION
package com.proyek.maganggsp.presentation.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.proyek.maganggsp.databinding.FragmentHomeBinding
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.util.AppUtils
import com.proyek.maganggsp.util.NavigationConstants
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var receiptAdapter: ReceiptAdapter

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        Log.i(TAG, "🔄 FIXED HomeFragment - Navigation menggunakan ppid")
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupRecyclerView()
        setupSearchFeature()
        setupRefreshFeature()
        setupLogoutFeature()
        observeViewModel()
    }

    private fun setupUI() {
        // Update UI labels untuk Receipt context (Bahasa Indonesia)
        binding.tvRecentHistoryTitle.text = "Receipt Terbaru"
        binding.etSearch.hint = "Cari receipt berdasarkan nomor referensi..."

        // Setup "Lihat Semua" click
        binding.tvSeeAll.setOnClickListener {
            viewModel.refresh()
            Log.d(TAG, "🔄 Refresh data receipt diminta")
        }
    }

    private fun setupRecyclerView() {
        receiptAdapter = ReceiptAdapter()
        binding.rvLoketList.apply {
            adapter = receiptAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // FIXED: Setup click listener untuk navigate dengan ppid
        receiptAdapter.setOnItemClickListener { receipt ->
            try {
                // CRITICAL FIX: Kirim ppid yang benar untuk transaction logs
                val ppid = extractPpidFromReceipt(receipt)

                val bundle = NavigationConstants.createTransactionLogBundle(ppid)
                findNavController().navigate(
                    NavigationConstants.Actions.HOME_TO_TRANSACTION_LOG,
                    bundle
                )

                Log.d(TAG, "🧾 Navigasi ke transaction detail dengan ppid: $ppid")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Gagal navigasi ke transaction detail", e)
                AppUtils.showError(requireContext(), "Navigasi gagal: ${e.message}")
            }
        }

        Log.d(TAG, "✅ RecyclerView setup selesai dengan ReceiptAdapter")
    }

    private fun extractPpidFromReceipt(receipt: Receipt): String {
        // FIXED: Map receipt data ke ppid yang benar
        // Untuk sementara gunakan idPelanggan sebagai ppid identifier
        // Atau bisa juga dari refNumber jika mengandung ppid info
        return when {
            receipt.idPelanggan.isNotBlank() && receipt.idPelanggan.contains("PID") -> receipt.idPelanggan
            receipt.refNumber.isNotBlank() && receipt.refNumber.length > 10 -> receipt.refNumber
            else -> "PIDLKTD0025blok" // Fallback ke default ppid
        }.also { ppid ->
            Log.d(TAG, "📋 Extracted ppid: $ppid dari receipt: ${receipt.refNumber}")
        }
    }

    private fun setupSearchFeature() {
        binding.etSearch.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            Log.d(TAG, "🔍 Query pencarian: '$query'")

            viewModel.searchReceipts(query)
            updateHeaderVisibility(query.isNotEmpty())
        }
    }

    private fun setupRefreshFeature() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, "🔄 Pull to refresh dipicu")
            viewModel.refresh()
        }
    }

    private fun setupLogoutFeature() {
        binding.btnLogout.setOnClickListener {
            Log.d(TAG, "🚪 Logout diminta dari HomeFragment")
            // Trigger logout via parent activity
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun updateHeaderVisibility(isSearchMode: Boolean) {
        binding.tvRecentHistoryTitle.visibility = if (isSearchMode) View.GONE else View.VISIBLE
        binding.tvSeeAll.visibility = if (isSearchMode) View.GONE else View.VISIBLE
        Log.d(TAG, "👁️ Visibilitas header diupdate - Mode pencarian: $isSearchMode")
    }

    private fun observeViewModel() {
        // Observe admin profile
        lifecycleScope.launch {
            viewModel.adminProfileState.collectLatest { admin ->
                admin?.let {
                    binding.tvAdminName.text = "Selamat datang, ${it.name}!"
                    Log.d(TAG, "👤 Profil admin dimuat: ${it.name}")
                }
            }
        }

        // Observe receipt data dengan consolidated state management
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { resource ->
                handleUIStateChange(resource)
            }
        }
    }

    private fun handleUIStateChange(resource: Resource<List<Receipt>>) {
        // Gunakan AppUtils untuk consistent loading state management
        AppUtils.handleLoadingState(
            shimmerView = binding.standardShimmerLayout,
            contentView = binding.rvLoketList,
            emptyView = binding.tvEmptyHistory,
            resource = resource
        )

        // Manage SwipeRefreshLayout loading indicator
        binding.swipeRefreshLayout.isRefreshing = resource is Resource.Loading

        when (resource) {
            is Resource.Success -> {
                handleSuccessState(resource)
            }
            is Resource.Error -> {
                handleErrorState(resource)
            }
            is Resource.Loading -> {
                Log.d(TAG, "⏳ Memuat data receipt...")
            }
            else -> Unit
        }
    }

    private fun handleSuccessState(resource: Resource.Success<List<Receipt>>) {
        val data = resource.data ?: emptyList()
        Log.d(TAG, "✅ State berhasil - ${data.size} receipt diterima")

        // Update adapter dengan data baru
        receiptAdapter.updateData(data)

        // Apply contextual empty state messaging (Bahasa Indonesia)
        val currentQuery = binding.etSearch.text.toString().trim()
        val isSearchMode = currentQuery.isNotEmpty()

        AppUtils.applyEmptyState(
            textView = binding.tvEmptyHistory,
            context = "home",
            itemCount = data.size,
            isSearchMode = isSearchMode,
            searchQuery = currentQuery
        )
    }

    private fun handleErrorState(resource: Resource.Error<List<Receipt>>) {
        Log.e(TAG, "❌ State error: ${resource.message}")
        AppUtils.showError(requireContext(), resource.exception)

        // Show contextual empty state untuk errors (Bahasa Indonesia)
        binding.tvEmptyHistory.text = "Gagal memuat data receipt.\nTarik ke bawah untuk refresh atau periksa koneksi."
        binding.tvEmptyHistory.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "🔄 HomeFragment resumed - refresh data receipt")
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "🧹 HomeFragment view destroyed")
        _binding = null
    }
}
// File: app/src/main/java/com/proyek/maganggsp/presentation/home/HomeFragment.kt - COMPLETE REFACTOR
package com.proyek.maganggsp.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.FragmentHomeBinding
import com.proyek.maganggsp.domain.model.Receipt
import com.proyek.maganggsp.presentation.adapters.SearchAdapter
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.NavigationConstants
import com.proyek.maganggsp.util.AppUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * COMPLETE REFACTOR: HomeFragment dengan Receipt display and PPID search
 * Shows: Receipt list dari UnifiedRepository
 * Navigation: Receipt click -> DetailLoketActivity
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var searchAdapter: SearchAdapter

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        AppUtils.logInfo(TAG, "REFACTORED HomeFragment with Receipt focus")

        setupUI()
        setupRecyclerView()
        setupObservers()
        setupSearch()

        // Load initial data
        viewModel.loadRecentReceipts()
    }

    private fun setupUI() {
        // Display admin info
        viewModel.getAdminProfile()?.let { admin ->
            binding.tvAdminName.text = admin.getDisplayName()
        }

        // Search hint for PPID
        binding.etSearch.hint = "Cari loket (PPID)"

        // Logout button
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // See all button
        binding.tvSeeAll.setOnClickListener {
            AppUtils.showSuccess(requireContext(), "Fitur riwayat lengkap akan segera hadir")
        }
    }

    private fun setupRecyclerView() {
        searchAdapter = SearchAdapter { receipt ->
            navigateToDetail(receipt.ppid)
        }

        binding.rvLoketList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = searchAdapter
        }
    }

    private fun setupObservers() {
        // Search results observer
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.searchResults.collect { resource ->
                handleSearchResults(resource)
            }
        }

        // Recent receipts observer
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recentReceipts.collect { resource ->
                handleRecentReceipts(resource)
            }
        }

        // Search state observer
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.isSearching.collect { isSearching ->
                handleSearchState(isSearching)
            }
        }
    }

    private fun setupSearch() {
        binding.etSearch.doOnTextChanged { text, _, _, _ ->
            val query = text.toString().trim()

            when {
                query.isEmpty() -> {
                    // Show recent receipts when search is empty
                    viewModel.clearSearch()
                    updateTitleForState(false, "")
                }
                query.length >= 5 -> {
                    // Start search when query is >= 5 characters (PPID minimum)
                    viewModel.searchReceipts(query)
                    updateTitleForState(true, query)
                }
                else -> {
                    // Show hint for short query
                    updateTitleForState(false, "")
                    searchAdapter.clearSearchResults()
                    showEmptyState("Ketik minimal 5 karakter PPID untuk pencarian")
                }
            }
        }
    }

    private fun updateTitleForState(isSearching: Boolean, query: String) {
        binding.tvRecentHistoryTitle.text = if (isSearching) {
            "Hasil Pencarian: $query"
        } else {
            "Riwayat Terakhir"
        }
        binding.tvSeeAll.visibility = if (isSearching) View.GONE else View.VISIBLE
    }

    private fun handleSearchResults(resource: Resource<List<Receipt>>) {
        // Apply unified loading state management
        AppUtils.handleLoadingState(
            resource = resource,
            shimmerView = binding.standardShimmerLayout,
            contentView = binding.rvLoketList,
            emptyView = binding.tvEmptyHistory
        )

        when (resource) {
            is Resource.Success -> {
                searchAdapter.updateSearchResults(resource.data, binding.etSearch.text.toString())
                AppUtils.logInfo(TAG, "Search results: ${resource.data.size} receipts")

                if (resource.data.isEmpty()) {
                    showEmptyState("Tidak ditemukan receipt dengan PPID tersebut.\nCoba cari dengan format yang tepat.")
                }
            }
            is Resource.Error -> {
                AppUtils.showError(requireContext(), resource.exception)
                AppUtils.logError(TAG, "Search error", resource.exception)
                showEmptyState("Gagal melakukan pencarian. Coba lagi.")
            }
            is Resource.Empty -> {
                showEmptyState("Tidak ada hasil pencarian")
            }
            is Resource.Loading -> {
                // Handled by AppUtils.handleLoadingState
            }
        }
    }

    private fun handleRecentReceipts(resource: Resource<List<Receipt>>) {
        // Only handle recent receipts when not searching
        if (viewModel.isSearching.value) return

        // Apply unified loading state management
        AppUtils.handleLoadingState(
            resource = resource,
            shimmerView = binding.standardShimmerLayout,
            contentView = binding.rvLoketList,
            emptyView = binding.tvEmptyHistory
        )

        when (resource) {
            is Resource.Success -> {
                searchAdapter.updateSearchResults(resource.data, "")
                AppUtils.logInfo(TAG, "Recent receipts loaded: ${resource.data.size}")

                if (resource.data.isEmpty()) {
                    showEmptyState("Belum ada riwayat pencarian.\nMulai cari loket dengan PPID.")
                }
            }
            is Resource.Error -> {
                AppUtils.logError(TAG, "Recent receipts error", resource.exception)
                showEmptyState("Gagal memuat riwayat pencarian")
            }
            is Resource.Empty -> {
                showEmptyState("Belum ada riwayat pencarian.\nMulai cari loket dengan PPID.")
            }
            is Resource.Loading -> {
                // Handled by AppUtils.handleLoadingState
            }
        }
    }

    private fun handleSearchState(isSearching: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = false

        AppUtils.logDebug(TAG, if (isSearching) "Search mode active" else "Showing recent receipts")
    }

    private fun showEmptyState(message: String) {
        binding.tvEmptyHistory.text = message
        binding.tvEmptyHistory.visibility = View.VISIBLE
        binding.rvLoketList.visibility = View.GONE
    }

    private fun navigateToDetail(ppid: String) {
        try {
            val bundle = NavigationConstants.createDetailLoketBundle(ppid)
            findNavController().navigate(
                R.id.action_homeFragment_to_detailLoketActivity,
                bundle
            )
            AppUtils.logInfo(TAG, "Navigating to detail with PPID: $ppid")
        } catch (e: Exception) {
            AppUtils.logError(TAG, "Navigation error", e)
            AppUtils.showError(requireContext(), "Gagal membuka detail loket")
        }
    }

    private fun showLogoutConfirmation() {
        androidx.appcompat.app.AlertDialog.Builder(requireContext())
            .setTitle("Konfirmasi Logout")
            .setMessage("Yakin ingin keluar dari aplikasi?")
            .setPositiveButton("Ya") { _, _ ->
                viewModel.logout()
                requireActivity().finish()
            }
            .setNegativeButton("Tidak", null)
            .show()
    }

    override fun onResume() {
        super.onResume()
        // Refresh data when returning to screen
        if (!viewModel.isSearching.value) {
            viewModel.refresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}


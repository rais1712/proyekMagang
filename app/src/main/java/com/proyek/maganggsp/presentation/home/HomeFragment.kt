// File: app/src/main/java/com/proyek/maganggsp/presentation/home/HomeFragment.kt - MVP CORE
package com.proyek.maganggsp.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.widget.doOnTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.FragmentHomeBinding
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.presentation.adapters.LoketSearchAdapter
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.NavigationConstants
import com.proyek.maganggsp.util.AppUtils
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * MVP CORE: HomeFragment dengan search functionality dan navigation ke DetailLoketActivity
 */
@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var searchAdapter: LoketSearchAdapter

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        setupObservers()
        setupSearch()

        // Load initial data
        viewModel.loadRecentLokets()
    }

    private fun setupUI() {
        // Display admin info
        viewModel.getAdminProfile()?.let { admin ->
            binding.tvAdminName.text = admin.getDisplayName()
        }

        // Update search hint untuk phone number
        binding.etSearch.hint = "Cari loket (No. Telepon)"

        // Logout button
        binding.btnLogout.setOnClickListener {
            showLogoutConfirmation()
        }

        // See all button
        binding.tvSeeAll.setOnClickListener {
            // TODO: Navigate to history when implemented
            AppUtils.showSuccess(requireContext(), "Fitur riwayat akan segera hadir")
        }
    }

    private fun setupRecyclerView() {
        searchAdapter = LoketSearchAdapter(
            onItemClick = { loket ->
                navigateToLoketDetail(loket.ppid)
            }
        )

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

        // Recent lokets observer
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.recentLokets.collect { resource ->
                handleRecentLokets(resource)
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

            if (query.isEmpty()) {
                // Show recent lokets when search is empty
                viewModel.clearSearch()
                binding.tvRecentHistoryTitle.text = "Riwayat Terakhir"
                binding.tvSeeAll.visibility = View.VISIBLE
            } else if (query.length >= 3) {
                // Start search when query is >= 3 characters
                viewModel.searchLoket(query)
                binding.tvRecentHistoryTitle.text = "Hasil Pencarian"
                binding.tvSeeAll.visibility = View.GONE
            } else {
                // Show hint for short query
                binding.tvRecentHistoryTitle.text = "Ketik minimal 3 angka"
                binding.tvSeeAll.visibility = View.GONE
                searchAdapter.submitList(emptyList())
                binding.tvEmptyHistory.visibility = View.VISIBLE
                binding.tvEmptyHistory.text = "Ketik minimal 3 angka untuk pencarian"
            }
        }
    }

    private fun handleSearchResults(resource: Resource<List<Loket>>) {
        resource.applyToStandardLoadingViews(
            shimmerView = binding.standardShimmerLayout,
            contentView = binding.rvLoketList,
            emptyView = binding.tvEmptyHistory
        )

        when (resource) {
            is Resource.Success -> {
                searchAdapter.submitList(resource.data)
                AppUtils.logInfo(TAG, "Search results: ${resource.data.size} lokets")
            }
            is Resource.Error -> {
                AppUtils.showError(requireContext(), resource.exception)
                AppUtils.logError(TAG, "Search error: ${resource.exception.message}")
            }
            is Resource.Empty -> {
                binding.tvEmptyHistory.text = "Tidak ditemukan loket dengan nomor tersebut"
            }
            is Resource.Loading -> {
                // Handled by applyToStandardLoadingViews
            }
        }
    }

    private fun handleRecentLokets(resource: Resource<List<Loket>>) {
        // Only handle recent lokets when not searching
        if (viewModel.isSearching.value) return

        resource.applyToStandardLoadingViews(
            shimmerView = binding.standardShimmerLayout,
            contentView = binding.rvLoketList,
            emptyView = binding.tvEmptyHistory
        )

        when (resource) {
            is Resource.Success -> {
                searchAdapter.submitList(resource.data)
                AppUtils.logInfo(TAG, "Recent lokets loaded: ${resource.data.size}")
            }
            is Resource.Error -> {
                AppUtils.logError(TAG, "Recent lokets error: ${resource.exception.message}")
                binding.tvEmptyHistory.text = "Gagal memuat riwayat pencarian"
            }
            is Resource.Empty -> {
                binding.tvEmptyHistory.text = "Belum ada riwayat pencarian.\nMulai cari loket dengan nomor telepon."
            }
            is Resource.Loading -> {
                // Handled by applyToStandardLoadingViews
            }
        }
    }

    private fun handleSearchState(isSearching: Boolean) {
        binding.swipeRefreshLayout.isRefreshing = false

        if (isSearching) {
            AppUtils.logDebug(TAG, "Search mode active")
        } else {
            AppUtils.logDebug(TAG, "Showing recent lokets")
        }
    }

    private fun navigateToLoketDetail(ppid: String) {
        try {
            val bundle = NavigationConstants.createTransactionLogBundle(ppid)
            findNavController().navigate(
                R.id.action_homeFragment_to_transactionLogActivity,
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
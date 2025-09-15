// File: app/src/main/java/com/proyek/maganggsp/presentation/home/HomeFragment.kt - UPDATED FOR LOKET
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
import com.proyek.maganggsp.domain.model.LoketSearchHistory
import com.proyek.maganggsp.presentation.adapters.LoketSearchAdapter
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
    private lateinit var loketSearchAdapter: LoketSearchAdapter

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        Log.i(TAG, "UPDATED HomeFragment - Loket search system implemented")
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
        // Update UI labels untuk Loket search context (Bahasa Indonesia)
        binding.tvRecentHistoryTitle.text = "Riwayat Pencarian"
        binding.etSearch.hint = "Cari loket dengan PPID atau nama loket..."

        // Setup "Lihat Semua" click
        binding.tvSeeAll.setOnClickListener {
            viewModel.refresh()
            Log.d(TAG, "Refresh recent lokets diminta")
        }
    }

    private fun setupRecyclerView() {
        loketSearchAdapter = LoketSearchAdapter()
        binding.rvLoketList.apply {
            adapter = loketSearchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Setup click listener untuk navigate ke detail loket
        loketSearchAdapter.setOnItemClickListener { loketHistory ->
            try {
                navigateToLoketDetail(loketHistory)
            } catch (e: Exception) {
                Log.e(TAG, "Gagal navigasi ke detail loket", e)
                AppUtils.showError(requireContext(), "Navigasi gagal: ${e.message}")
            }
        }

        Log.d(TAG, "RecyclerView setup selesai dengan LoketSearchAdapter")
    }

    private fun navigateToLoketDetail(loketHistory: LoketSearchHistory) {
        val ppid = loketHistory.ppid

        // Validate ppid sebelum navigasi
        if (ppid.isBlank()) {
            AppUtils.showError(requireContext(), "PPID tidak valid")
            return
        }

        Log.d(TAG, "Navigasi ke detail loket dengan ppid: $ppid")

        try {
            val bundle = NavigationConstants.createTransactionLogBundle(ppid)
            findNavController().navigate(
                NavigationConstants.Actions.HOME_TO_TRANSACTION_LOG,
                bundle
            )
            Log.d(TAG, "Successfully navigated to loket detail")
        } catch (e: Exception) {
            Log.e(TAG, "Navigation error", e)
            AppUtils.showError(requireContext(), "Gagal membuka detail loket")
        }
    }

    private fun setupSearchFeature() {
        binding.etSearch.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            Log.d(TAG, "Search query: '$query'")

            viewModel.searchLokets(query)
            updateHeaderVisibility(query.isNotEmpty())
        }
    }

    private fun setupRefreshFeature() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, "Pull to refresh triggered")
            viewModel.refresh()
        }
    }

    private fun setupLogoutFeature() {
        binding.btnLogout.setOnClickListener {
            Log.d(TAG, "Logout requested from HomeFragment")
            // Trigger logout via parent activity
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun updateHeaderVisibility(isSearchMode: Boolean) {
        binding.tvRecentHistoryTitle.visibility = if (isSearchMode) View.GONE else View.VISIBLE
        binding.tvSeeAll.visibility = if (isSearchMode) View.GONE else View.VISIBLE
        Log.d(TAG, "Header visibility updated - Search mode: $isSearchMode")
    }

    private fun observeViewModel() {
        // Observe admin profile
        lifecycleScope.launch {
            viewModel.adminProfileState.collectLatest { admin ->
                admin?.let {
                    binding.tvAdminName.text = "Halo, ${it.name}!"
                    Log.d(TAG, "Admin profile loaded: ${it.name}")
                }
            }
        }

        // Observe loket search history
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { resource ->
                handleUIStateChange(resource)
            }
        }
    }

    private fun handleUIStateChange(resource: Resource<List<LoketSearchHistory>>) {
        // Use AppUtils untuk consistent loading state management
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
                Log.d(TAG, "Loading loket search data...")
            }
            else -> Unit
        }
    }

    private fun handleSuccessState(resource: Resource.Success<List<LoketSearchHistory>>) {
        val data = resource.data ?: emptyList()
        Log.d(TAG, "Success state - ${data.size} loket history items received")

        // Update adapter dengan data baru
        loketSearchAdapter.updateData(data)

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

    private fun handleErrorState(resource: Resource.Error<List<LoketSearchHistory>>) {
        Log.e(TAG, "Error state: ${resource.message}")
        AppUtils.showError(requireContext(), resource.exception)

        // Show contextual empty state for errors (Bahasa Indonesia)
        binding.tvEmptyHistory.text = "Gagal memuat riwayat pencarian.\nTarik ke bawah untuk refresh atau periksa koneksi."
        binding.tvEmptyHistory.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "HomeFragment resumed - refresh loket search data")
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "HomeFragment view destroyed")
        _binding = null
    }
}
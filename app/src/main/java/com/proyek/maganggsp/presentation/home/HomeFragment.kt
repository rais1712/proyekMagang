// File: app/src/main/java/com/proyek/maganggsp/presentation/home/HomeFragment.kt
package com.proyek.maganggsp.presentation.home

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.proyek.maganggsp.R
import com.proyek.maganggsp.databinding.FragmentHomeBinding
import com.proyek.maganggsp.util.Resource
import com.proyek.maganggsp.util.applyToStandardLoadingViews
import com.proyek.maganggsp.util.EmptyStateHandler
import com.proyek.maganggsp.util.FeatureFlags
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var loketAdapter: HistoryAdapter

    companion object {
        private const val TAG = "HomeFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // 🚩 FEATURE FLAGS: Log current configuration
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.i(TAG, "🚩 HomeFragment created with feature flags enabled")
            Log.d(TAG, "Search enabled: ${FeatureFlags.ENABLE_SEARCH_LOKET}")
            Log.d(TAG, "Recent history enabled: ${FeatureFlags.ENABLE_RECENT_HISTORY}")
            Log.d(TAG, "Detail navigation enabled: ${FeatureFlags.ENABLE_LOKET_DETAIL_NAVIGATION}")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupUI()
        setupRecyclerView()
        setupSearchFeature()
        setupRefreshFeature()
        observeViewModel()
    }

    private fun setupUI() {
        // 🚩 FEATURE FLAGS: Conditional UI setup

        // Search functionality
        if (FeatureFlags.ENABLE_SEARCH_LOKET) {
            setupSearchListener()
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Search functionality enabled")
            }
        } else {
            binding.etSearch.isEnabled = false
            binding.etSearch.hint = "Fitur pencarian sedang dikembangkan"
            binding.cardSearch.alpha = 0.5f

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Search functionality disabled")
            }
        }

        // Recent History section
        if (FeatureFlags.ENABLE_RECENT_HISTORY) {
            binding.tvRecentHistoryTitle.isVisible = true
            binding.tvSeeAll.isVisible = true

            // Set up "Lihat Semua" click listener
            binding.tvSeeAll.setOnClickListener {
                if (FeatureFlags.ENABLE_HISTORY_FRAGMENT) {
                    try {
                        findNavController().navigate(R.id.historyFragment)
                        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                            Log.d(TAG, "🚩 Navigated to history fragment")
                        }
                    } catch (e: Exception) {
                        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                            Log.e(TAG, "🚩 Failed to navigate to history", e)
                        }
                        Toast.makeText(context, "Fitur riwayat sedang dikembangkan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(context, "Fitur riwayat sedang dikembangkan", Toast.LENGTH_SHORT).show()
                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.w(TAG, "🚩 History navigation blocked by feature flag")
                    }
                }
            }

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Recent history section enabled")
            }
        } else {
            binding.tvRecentHistoryTitle.isVisible = false
            binding.tvSeeAll.isVisible = false

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Recent history section disabled")
            }
        }
    }

    private fun setupRecyclerView() {
        loketAdapter = HistoryAdapter()
        binding.rvLoketList.apply {
            adapter = loketAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // 🚩 FEATURE FLAGS: Conditional item click handling
        if (FeatureFlags.ENABLE_LOKET_DETAIL_NAVIGATION) {
            loketAdapter.setOnItemClickListener { loket ->
                try {
                    val action = HomeFragmentDirections.actionHomeFragmentToDetailLoketActivity(loket.noLoket)
                    findNavController().navigate(action)

                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.d(TAG, "🚩 Navigated to detail for loket: ${loket.noLoket}")
                    }
                } catch (e: Exception) {
                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.e(TAG, "🚩 Failed to navigate to detail", e)
                    }
                    Toast.makeText(context, "Detail loket sedang dikembangkan", Toast.LENGTH_SHORT).show()
                }
            }

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Detail navigation enabled")
            }
        } else {
            // Disable item clicks
            loketAdapter.setOnItemClickListener {
                Toast.makeText(context, "Detail loket sedang dikembangkan", Toast.LENGTH_SHORT).show()
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.w(TAG, "🚩 Detail navigation blocked by feature flag")
                }
            }

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Detail navigation disabled")
            }
        }
    }

    private fun setupSearchFeature() {
        if (!FeatureFlags.ENABLE_SEARCH_LOKET) return

        // Search functionality only enabled if feature flag allows
        setupSearchListener()
    }

    private fun setupRefreshFeature() {
        // 🚩 FEATURE FLAGS: Conditional pull-to-refresh
        if (FeatureFlags.ENABLE_PULL_TO_REFRESH) {
            binding.swipeRefreshLayout.setOnRefreshListener {
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.d(TAG, "🚩 Pull to refresh triggered")
                }
                viewModel.refresh()
            }

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Pull-to-refresh enabled")
            }
        } else {
            binding.swipeRefreshLayout.isEnabled = false
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Pull-to-refresh disabled")
            }
        }
    }

    private fun setupSearchListener() {
        if (!FeatureFlags.ENABLE_SEARCH_LOKET) return

        binding.etSearch.addTextChangedListener { editable ->
            val query = editable.toString().trim()

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Search query: '$query'")
            }

            // 🚩 FEATURE FLAGS: Only search if real data loading is enabled
            if (FeatureFlags.ENABLE_REAL_DATA_LOADING) {
                viewModel.searchLoket(query)

                // Update visibility header berdasarkan mode pencarian
                updateHeaderVisibility(query.isNotEmpty())
            } else {
                // Show mock search behavior
                Toast.makeText(context, "Pencarian sedang dikembangkan", Toast.LENGTH_SHORT).show()
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.w(TAG, "🚩 Search blocked - real data loading disabled")
                }
            }
        }
    }

    private fun updateHeaderVisibility(isSearchMode: Boolean) {
        // 🚩 FEATURE FLAGS: Only update if recent history is enabled
        if (FeatureFlags.ENABLE_RECENT_HISTORY) {
            binding.tvRecentHistoryTitle.visibility = if (isSearchMode) View.GONE else View.VISIBLE
            binding.tvSeeAll.visibility = if (isSearchMode) View.GONE else View.VISIBLE

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Header visibility updated - Search mode: $isSearchMode")
            }
        }
    }

    private fun observeViewModel() {
        // Observe admin profile
        lifecycleScope.launch {
            viewModel.adminProfileState.collectLatest { admin ->
                admin?.let {
                    binding.tvAdminName.text = getString(R.string.welcome_admin, it.name)

                    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                        Log.d(TAG, "🚩 Admin profile loaded: ${it.name}")
                    }
                }
            }
        }

        // Observe UI state with feature flags
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { resource ->
                handleUIStateChange(resource)
            }
        }
    }

    private fun handleUIStateChange(resource: Resource<List<com.proyek.maganggsp.domain.model.Loket>>) {
        // 🚩 FEATURE FLAGS: Conditional loading management
        if (FeatureFlags.ENABLE_SHIMMER_LOADING) {
            // Use standardized loading management
            resource.applyToStandardLoadingViews(
                shimmerView = binding.standardShimmerLayout,
                contentView = binding.rvLoketList,
                emptyView = binding.tvEmptyHistory
            )
        } else {
            // Simple loading without shimmer
            binding.standardShimmerLayout.isVisible = false
            binding.rvLoketList.isVisible = resource is Resource.Success && (resource.data?.isNotEmpty() == true)
            binding.tvEmptyHistory.isVisible = resource is Resource.Success && (resource.data?.isEmpty() == true)

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Shimmer loading disabled - using simple loading")
            }
        }

        // Manage SwipeRefreshLayout loading indicator
        if (FeatureFlags.ENABLE_PULL_TO_REFRESH) {
            binding.swipeRefreshLayout.isRefreshing = resource is Resource.Loading
        }

        when (resource) {
            is Resource.Success -> {
                handleSuccessState(resource)
            }
            is Resource.Error -> {
                handleErrorState(resource)
            }
            is Resource.Loading -> {
                if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                    Log.d(TAG, "🚩 Loading state active")
                }
            }
            else -> Unit
        }
    }

    private fun handleSuccessState(resource: Resource.Success<List<com.proyek.maganggsp.domain.model.Loket>>) {
        val data = resource.data ?: emptyList()

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 Success state - ${data.size} items received")
        }

        // Update adapter
        loketAdapter.differ.submitList(data)

        // 🚩 FEATURE FLAGS: Smart contextual empty state
        if (FeatureFlags.ENABLE_FALLBACK_EMPTY_STATES) {
            val currentQuery = binding.etSearch.text.toString().trim()
            val isSearchMode = currentQuery.isNotEmpty()

            EmptyStateHandler.applySmartEmptyState(
                textView = binding.tvEmptyHistory,
                context = "home",
                itemCount = data.size,
                isSearchMode = isSearchMode,
                searchQuery = currentQuery,
                hasNetworkConnection = true // TODO: Add actual network check
            )
        } else {
            // Simple empty state
            binding.tvEmptyHistory.isVisible = data.isEmpty()
            if (data.isEmpty()) {
                binding.tvEmptyHistory.text = "Tidak ada data"
            }

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Using simple empty state - smart empty states disabled")
            }
        }
    }

    private fun handleErrorState(resource: Resource.Error<List<com.proyek.maganggsp.domain.model.Loket>>) {
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.e(TAG, "🚩 Error state: ${resource.message}")
        }

        // 🚩 FEATURE FLAGS: Conditional error handling
        if (FeatureFlags.ENABLE_DETAILED_ERROR_MESSAGES) {
            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
        } else {
            Toast.makeText(requireContext(), "Terjadi kesalahan", Toast.LENGTH_SHORT).show()
        }

        // Show network-aware empty state
        if (FeatureFlags.ENABLE_FALLBACK_EMPTY_STATES) {
            EmptyStateHandler.applyEmptyState(
                textView = binding.tvEmptyHistory,
                emptyStateType = EmptyStateHandler.EmptyStateType.NoConnection,
                isVisible = true
            )
        } else {
            binding.tvEmptyHistory.isVisible = true
            binding.tvEmptyHistory.text = "Gagal memuat data"
        }
    }

    override fun onResume() {
        super.onResume()

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 HomeFragment resumed")
        }

        // 🚩 FEATURE FLAGS: Refresh data only if enabled
        if (FeatureFlags.ENABLE_REAL_DATA_LOADING) {
            viewModel.refresh()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 HomeFragment view destroyed")
        }

        _binding = null
    }
}
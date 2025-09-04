// File: app/src/main/java/com/proyek/maganggsp/presentation/home/HomeFragment.kt - FINAL REFACTORED
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
        Log.i(TAG, "🔄 FINAL REFACTORED HomeFragment created for Receipt data structure")
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
        // Update UI labels for Receipt context
        binding.tvRecentHistoryTitle.text = "Recent Receipts"
        binding.etSearch.hint = "Search receipts by reference number..."

        // Setup "Lihat Semua" click
        binding.tvSeeAll.setOnClickListener {
            viewModel.refresh()
            Log.d(TAG, "🔄 Refresh receipts data requested")
        }
    }

    private fun setupRecyclerView() {
        receiptAdapter = ReceiptAdapter()
        binding.rvLoketList.apply {
            adapter = receiptAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Setup click listener for receipt items -> navigate to transaction log detail
        receiptAdapter.setOnItemClickListener { receipt ->
            try {
                // Navigate using receipt's reference number as identifier
                val action = HomeFragmentDirections.actionHomeFragmentToDetailLoketActivity(
                    receipt.refNumber // Pass refNumber as the identifier for transaction logs
                )
                findNavController().navigate(action)
                Log.d(TAG, "🧾 Navigated to transaction detail for receipt: ${receipt.refNumber}")
            } catch (e: Exception) {
                Log.e(TAG, "❌ Failed to navigate to transaction detail", e)
                AppUtils.showError(requireContext(), "Navigation failed: ${e.message}")
            }
        }

        Log.d(TAG, "✅ RecyclerView setup completed with ReceiptAdapter")
    }

    private fun setupSearchFeature() {
        binding.etSearch.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            Log.d(TAG, "🔍 Search query: '$query'")

            viewModel.searchReceipts(query)
            updateHeaderVisibility(query.isNotEmpty())
        }
    }

    private fun setupRefreshFeature() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            Log.d(TAG, "🔄 Pull to refresh triggered")
            viewModel.refresh()
        }
    }

    private fun setupLogoutFeature() {
        // Add logout functionality to home screen
        binding.btnLogout.setOnClickListener {
            Log.d(TAG, "🚪 Logout requested from HomeFragment")
            // Trigger logout via parent activity or navigation
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun updateHeaderVisibility(isSearchMode: Boolean) {
        binding.tvRecentHistoryTitle.visibility = if (isSearchMode) View.GONE else View.VISIBLE
        binding.tvSeeAll.visibility = if (isSearchMode) View.GONE else View.VISIBLE
        Log.d(TAG, "👁️ Header visibility updated - Search mode: $isSearchMode")
    }

    private fun observeViewModel() {
        // Observe admin profile
        lifecycleScope.launch {
            viewModel.adminProfileState.collectLatest { admin ->
                admin?.let {
                    binding.tvAdminName.text = "Welcome, ${it.name}!"
                    Log.d(TAG, "👤 Admin profile loaded: ${it.name}")
                }
            }
        }

        // Observe receipt data with consolidated state management
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { resource ->
                handleUIStateChange(resource)
            }
        }
    }

    private fun handleUIStateChange(resource: Resource<List<Receipt>>) {
        // Use AppUtils for consistent loading state management
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
                Log.d(TAG, "⏳ Loading receipt data...")
            }
            else -> Unit
        }
    }

    private fun handleSuccessState(resource: Resource.Success<List<Receipt>>) {
        val data = resource.data ?: emptyList()
        Log.d(TAG, "✅ Success state - ${data.size} receipts received")

        // Update adapter with new data
        receiptAdapter.updateData(data)

        // Apply contextual empty state messaging
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
        Log.e(TAG, "❌ Error state: ${resource.message}")
        AppUtils.showError(requireContext(), resource.exception)

        // Show contextual empty state for errors
        binding.tvEmptyHistory.text = "Failed to load receipt data.\nPull down to refresh or check your connection."
        binding.tvEmptyHistory.visibility = View.VISIBLE
    }

    override fun onResume() {
        super.onResume()
        Log.d(TAG, "🔄 HomeFragment resumed - refreshing receipt data")
        viewModel.refresh()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "🧹 HomeFragment view destroyed")
        _binding = null
    }
}
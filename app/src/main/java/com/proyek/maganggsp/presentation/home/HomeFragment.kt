// File: app/src/main/java/com/proyek/maganggsp/presentation/home/HomeFragment.kt
package com.proyek.maganggsp.presentation.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var loketAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupSearchListener()
        observeViewModel()

        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refresh()
        }
    }

    private fun setupRecyclerView() {
        loketAdapter = HistoryAdapter()
        binding.rvLoketList.apply {
            adapter = loketAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        loketAdapter.setOnItemClickListener { loket ->
            val action = HomeFragmentDirections.actionHomeFragmentToDetailLoketActivity(loket.noLoket)
            findNavController().navigate(action)
        }
    }

    private fun setupSearchListener() {
        binding.etSearch.addTextChangedListener { editable ->
            val query = editable.toString().trim()
            viewModel.searchLoket(query)

            // Update visibility header berdasarkan mode pencarian
            updateHeaderVisibility(query.isNotEmpty())
        }
    }

    private fun updateHeaderVisibility(isSearchMode: Boolean) {
        binding.tvRecentHistoryTitle.visibility = if (isSearchMode) View.GONE else View.VISIBLE
        binding.tvSeeAll.visibility = if (isSearchMode) View.GONE else View.VISIBLE
    }

    private fun observeViewModel() {
        // Mengamati admin profile
        lifecycleScope.launch {
            viewModel.adminProfileState.collectLatest { admin ->
                admin?.let {
                    binding.tvAdminName.text = getString(R.string.welcome_admin, it.name)
                }
            }
        }

        // ENHANCED: Using standardized loading + smart empty states
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { resource ->
                // Standardized loading management
                resource.applyToStandardLoadingViews(
                    shimmerView = binding.standardShimmerLayout,
                    contentView = binding.rvLoketList,
                    emptyView = binding.tvEmptyHistory
                )

                // Manage SwipeRefreshLayout loading indicator
                binding.swipeRefreshLayout.isRefreshing = resource is Resource.Loading

                when (resource) {
                    is Resource.Success -> {
                        val data = resource.data ?: emptyList()
                        loketAdapter.differ.submitList(data)

                        // ENHANCED: Smart contextual empty state
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
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()

                        // Show network-aware empty state
                        EmptyStateHandler.applyEmptyState(
                            textView = binding.tvEmptyHistory,
                            emptyStateType = EmptyStateHandler.EmptyStateType.NoConnection,
                            isVisible = true
                        )
                    }
                    else -> Unit // Loading dan Empty state sudah dihandle oleh extension
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
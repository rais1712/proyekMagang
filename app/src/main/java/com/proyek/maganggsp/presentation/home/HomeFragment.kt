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

        // Mengamati UI State (satu observer untuk semua)
        lifecycleScope.launch {
            viewModel.uiState.collectLatest { resource ->
                binding.shimmerLayoutHistory.visibility = if (resource is Resource.Loading) View.VISIBLE else View.GONE
                binding.rvLoketList.visibility = if (resource is Resource.Success && resource.data?.isNotEmpty() == true) View.VISIBLE else View.GONE
                binding.tvEmptyHistory.visibility = if (resource is Resource.Success && resource.data?.isEmpty() == true) View.VISIBLE else View.GONE
                binding.swipeRefreshLayout.isRefreshing = resource is Resource.Loading

                when (resource) {
                    is Resource.Success -> {
                        loketAdapter.differ.submitList(resource.data ?: emptyList())
                        // Context-aware empty state message
                        binding.tvEmptyHistory.text = if (binding.etSearch.text.toString().trim().isNotEmpty()) {
                            getString(R.string.no_search_results)
                        } else {
                            getString(R.string.no_recent_history)
                        }
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                    }
                    else -> Unit // Handle Loading dan Empty state dengan visibility di atas
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
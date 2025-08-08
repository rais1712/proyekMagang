package com.proyek.maganggsp.presentation.home

import android.content.Intent
import android.os.Bundle
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
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.presentation.detail_loket.DetailLoketActivity
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HomeViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter
    private lateinit var searchAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupListeners()
        observeAdminProfile()
        observeRecentHistory()
        observeSearchResult()
    }

    private fun setupRecyclerViews() {
        historyAdapter = HistoryAdapter()
        searchAdapter = HistoryAdapter() // Menggunakan adapter yang sama

        binding.rvRecentHistory.apply {
            adapter = historyAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
        binding.rvSearchResult.apply {
            adapter = searchAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun setupListeners() {
        // Listener untuk pencarian
        binding.etSearch.addTextChangedListener { editable ->
            viewModel.onSearchQueryChanged(editable.toString())
        }

        // Listener untuk "Lihat Semua"
        binding.tvSeeAll.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_historyFragment)
        }

        // Listener untuk klik item di RecyclerView
        val itemClickListener = { loket: Loket ->
            val intent = Intent(requireActivity(), DetailLoketActivity::class.java).apply {
                putExtra("phone_number", loket.phoneNumber)
            }
            startActivity(intent)
        }
        historyAdapter.setOnItemClickListener(itemClickListener)
        searchAdapter.setOnItemClickListener(itemClickListener)

        // Listener untuk Swipe-to-Refresh
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshRecentHistory()
        }
    }

    private fun observeAdminProfile() {
        lifecycleScope.launch {
            viewModel.adminProfileState.collectLatest { admin ->
                admin?.let {
                    binding.tvAdminName.text = it.name
                }
            }
        }
    }

    private fun observeRecentHistory() {
        lifecycleScope.launch {
            viewModel.recentHistoryState.collectLatest { resource ->
                // Hanya kelola UI riwayat jika query pencarian kosong
                if (binding.etSearch.text.toString().trim().isEmpty()) {
                    when (resource) {
                        is Resource.Loading -> {
                            binding.shimmerLayoutHistory.startShimmer()
                            binding.shimmerLayoutHistory.isVisible = true
                            binding.rvRecentHistory.isVisible = false
                            binding.tvEmptyHistory.isVisible = false
                        }
                        is Resource.Success -> {
                            binding.shimmerLayoutHistory.stopShimmer()
                            binding.shimmerLayoutHistory.isVisible = false
                            binding.swipeRefreshLayout.isRefreshing = false

                            val data = resource.data ?: emptyList()
                            if (data.isEmpty()) {
                                binding.rvRecentHistory.isVisible = false
                                binding.tvEmptyHistory.isVisible = true
                            } else {
                                binding.rvRecentHistory.isVisible = true
                                binding.tvEmptyHistory.isVisible = false
                                historyAdapter.differ.submitList(data)
                            }
                        }
                        is Resource.Error -> {
                            binding.shimmerLayoutHistory.stopShimmer()
                            binding.shimmerLayoutHistory.isVisible = false
                            binding.swipeRefreshLayout.isRefreshing = false
                            binding.rvRecentHistory.isVisible = false
                            binding.tvEmptyHistory.isVisible = false
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                        }
                        is Resource.Empty -> {
                            binding.shimmerLayoutHistory.stopShimmer()
                            binding.shimmerLayoutHistory.isVisible = false
                            binding.rvRecentHistory.isVisible = false
                            binding.tvEmptyHistory.isVisible = true
                            binding.tvEmptyHistory.text = getString(R.string.no_recent_history)
                        }
                    }
                }
            }
        }
    }

    private fun observeSearchResult() {
        lifecycleScope.launch {
            viewModel.searchResultState.collectLatest { resource ->
                // Hanya kelola UI pencarian jika query TIDAK kosong
                if (binding.etSearch.text.toString().trim().isNotEmpty()) {
                    binding.groupHistory.isVisible = false
                    binding.groupSearch.isVisible = true

                    when (resource) {
                        is Resource.Loading -> {
                            binding.shimmerLayoutSearch.startShimmer()
                            binding.shimmerLayoutSearch.isVisible = true
                            binding.rvSearchResult.isVisible = false
                            binding.tvEmptySearch.isVisible = false
                        }
                        is Resource.Success -> {
                            binding.shimmerLayoutSearch.stopShimmer()
                            binding.shimmerLayoutSearch.isVisible = false

                            val data = resource.data ?: emptyList()
                            if (data.isEmpty()) {
                                binding.rvSearchResult.isVisible = false
                                binding.tvEmptySearch.isVisible = true
                            } else {
                                binding.rvSearchResult.isVisible = true
                                binding.tvEmptySearch.isVisible = false
                                searchAdapter.differ.submitList(data)
                            }
                        }
                        is Resource.Error -> {
                            binding.shimmerLayoutSearch.stopShimmer()
                            binding.shimmerLayoutSearch.isVisible = false
                            binding.rvSearchResult.isVisible = false
                            binding.tvEmptySearch.isVisible = false
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                        }
                        is Resource.Empty -> {
                            binding.shimmerLayoutSearch.stopShimmer()
                            binding.shimmerLayoutSearch.isVisible = false
                            binding.rvSearchResult.isVisible = false
                            binding.tvEmptySearch.isVisible = true
                            binding.tvEmptySearch.text = getString(R.string.no_search_results)
                        }
                    }
                } else {
                    binding.groupHistory.isVisible = true
                    binding.groupSearch.isVisible = false
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
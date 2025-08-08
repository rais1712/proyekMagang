package com.proyek.maganggsp.presentation.history

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import android.content.Intent
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.presentation.detail_loket.DetailLoketActivity
import com.proyek.maganggsp.databinding.FragmentHistoryBinding
import com.proyek.maganggsp.presentation.home.HistoryAdapter
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private val viewModel: HistoryViewModel by viewModels()
    private lateinit var historyAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupToolbar()
        setupRecyclerView()
        observeHistory()
    }

    private fun setupToolbar() {
        binding.toolbar.setNavigationOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter()
        binding.rvHistory.adapter = historyAdapter
        historyAdapter.setOnItemClickListener { loket ->
            // Mengarahkan ke DetailLoketActivity saat item diklik
            val intent = Intent(requireActivity(), DetailLoketActivity::class.java).apply {
                putExtra("phone_number", loket.phoneNumber)
            }
            startActivity(intent)
        }
    }

    private fun observeHistory() {
        lifecycleScope.launch {
            viewModel.historyState.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.shimmerLayout.startShimmer()
                        binding.shimmerLayout.isVisible = true
                        binding.rvHistory.isVisible = false
                        binding.tvEmpty.isVisible = false
                    }
                    is Resource.Success -> {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.isVisible = false

                        val data = resource.data ?: emptyList()
                        if (data.isEmpty()) {
                            binding.rvHistory.isVisible = false
                            binding.tvEmpty.isVisible = true
                        } else {
                            binding.rvHistory.isVisible = true
                            binding.tvEmpty.isVisible = false
                            historyAdapter.differ.submitList(data)
                        }
                    }
                    is Resource.Error -> {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.isVisible = false
                        binding.rvHistory.isVisible = false
                        binding.tvEmpty.isVisible = false
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                    }

                    else -> {
                        // Handle Resource.Empty atau state lainnya
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.isVisible = false
                        binding.rvHistory.isVisible = false
                        binding.tvEmpty.isVisible = false
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
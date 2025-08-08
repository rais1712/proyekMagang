package com.proyek.maganggsp.presentation.monitor.tabs

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.proyek.maganggsp.databinding.FragmentFlaggedLoketBinding
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.presentation.detail_loket.DetailLoketActivity
import com.proyek.maganggsp.presentation.home.HistoryAdapter
import com.proyek.maganggsp.presentation.monitor.MonitorViewModel
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FlaggedLoketFragment : Fragment() {

    private var _binding: FragmentFlaggedLoketBinding? = null
    private val binding get() = _binding!!

    // Gunakan activityViewModels() untuk berbagi ViewModel dengan MonitorFragment
    private val viewModel: MonitorViewModel by activityViewModels()
    private lateinit var loketAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFlaggedLoketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupRefreshListener()
        observeFlaggedLokets()
    }

    private fun setupRecyclerView() {
        loketAdapter = HistoryAdapter() // Kita bisa gunakan adapter yang sama
        binding.rvFlaggedLoket.apply {
            adapter = loketAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // Tambahkan click listener untuk navigasi ke Detail Loket
        loketAdapter.setOnItemClickListener { loket ->
            val intent = Intent(requireActivity(), DetailLoketActivity::class.java).apply {
                putExtra("phone_number", loket.phoneNumber)
            }
            startActivity(intent)
        }
    }

    private fun setupRefreshListener() {
        // Asumsi ada SwipeRefreshLayout di parent atau perlu ditambahkan method refresh
        // Untuk saat ini, kita bisa trigger refresh dari viewModel
        // viewModel.refreshFlaggedLokets() - method ini perlu ditambahkan di MonitorViewModel
    }

    private fun observeFlaggedLokets() {
        lifecycleScope.launch {
            viewModel.flaggedLoketsState.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.shimmerLayout.startShimmer()
                        binding.shimmerLayout.isVisible = true
                        binding.rvFlaggedLoket.isVisible = false
                        binding.tvEmpty.isVisible = false
                    }
                    is Resource.Success -> {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.isVisible = false

                        val data = resource.data ?: emptyList()
                        if (data.isEmpty()) {
                            binding.rvFlaggedLoket.isVisible = false
                            binding.tvEmpty.isVisible = true
                        } else {
                            binding.rvFlaggedLoket.isVisible = true
                            binding.tvEmpty.isVisible = false
                            loketAdapter.differ.submitList(data)
                        }
                    }
                    is Resource.Error -> {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.isVisible = false
                        binding.rvFlaggedLoket.isVisible = false
                        binding.tvEmpty.isVisible = false
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        // Handle Resource.Empty atau state lainnya
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.isVisible = false
                        binding.rvFlaggedLoket.isVisible = false
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
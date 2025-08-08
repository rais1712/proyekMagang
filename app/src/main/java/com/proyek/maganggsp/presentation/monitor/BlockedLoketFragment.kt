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
import com.proyek.maganggsp.databinding.FragmentBlockedLoketBinding
import com.proyek.maganggsp.domain.model.Loket
import com.proyek.maganggsp.presentation.detail_loket.DetailLoketActivity
import com.proyek.maganggsp.presentation.home.HistoryAdapter
import com.proyek.maganggsp.presentation.monitor.MonitorViewModel
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BlockedLoketFragment : Fragment() {

    private var _binding: FragmentBlockedLoketBinding? = null
    private val binding get() = _binding!!

    // Gunakan activityViewModels() untuk berbagi ViewModel
    private val viewModel: MonitorViewModel by activityViewModels()
    private lateinit var loketAdapter: HistoryAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBlockedLoketBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerView()
        setupRefreshListener()
        observeBlockedLokets()
    }

    private fun setupRecyclerView() {
        loketAdapter = HistoryAdapter()
        binding.rvBlockedLoket.apply {
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
        // viewModel.refreshBlockedLokets() - method ini perlu ditambahkan di MonitorViewModel
    }

    private fun observeBlockedLokets() {
        lifecycleScope.launch {
            viewModel.blockedLoketsState.collectLatest { resource ->
                when (resource) {
                    is Resource.Loading -> {
                        binding.shimmerLayout.startShimmer()
                        binding.shimmerLayout.isVisible = true
                        binding.rvBlockedLoket.isVisible = false
                        binding.tvEmpty.isVisible = false
                    }
                    is Resource.Success -> {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.isVisible = false

                        val data = resource.data ?: emptyList()
                        if (data.isEmpty()) {
                            binding.rvBlockedLoket.isVisible = false
                            binding.tvEmpty.isVisible = true
                        } else {
                            binding.rvBlockedLoket.isVisible = true
                            binding.tvEmpty.isVisible = false
                            loketAdapter.differ.submitList(data)
                        }
                    }
                    is Resource.Error -> {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.isVisible = false
                        binding.rvBlockedLoket.isVisible = false
                        binding.tvEmpty.isVisible = false
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_LONG).show()
                    }
                    else -> {
                        // Handle other states if necessary
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.isVisible = false
                        binding.rvBlockedLoket.isVisible = false
                        binding.tvEmpty.isVisible = true
                        binding.tvEmpty.text = "Tidak ada loket yang diblokir saat ini."
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

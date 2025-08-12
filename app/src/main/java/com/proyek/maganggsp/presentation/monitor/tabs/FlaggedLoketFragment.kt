package com.proyek.maganggsp.presentation.monitor.tabs

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.proyek.maganggsp.databinding.FragmentFlaggedLoketBinding
import com.proyek.maganggsp.presentation.home.HistoryAdapter // Asumsi menggunakan adapter ini
import com.proyek.maganggsp.presentation.monitor.MonitorFragmentDirections
import com.proyek.maganggsp.presentation.monitor.MonitorViewModel
import com.proyek.maganggsp.util.Resource
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

@AndroidEntryPoint
class FlaggedLoketFragment : Fragment() {

    private var _binding: FragmentFlaggedLoketBinding? = null
    private val binding get() = _binding!!

    private val viewModel: MonitorViewModel by activityViewModels()
    private lateinit var loketAdapter: HistoryAdapter // Menggunakan HistoryAdapter

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
        observeFlaggedLokets()
    }

// ... (kode import dan class lainnya)

    private fun setupRecyclerView() {
        loketAdapter = HistoryAdapter()
        binding.rvFlaggedLoket.apply {
            adapter = loketAdapter
            layoutManager = LinearLayoutManager(requireContext())
        }

        // --- PERUBAHAN UTAMA ADA DI SINI ---
        loketAdapter.setOnItemClickListener { loket ->
            // Membuat action dengan argumen 'noLoket' yang baru
            val action = MonitorFragmentDirections.actionMonitorFragmentToDetailLoketActivity(loket.noLoket)

            // Menjalankan navigasi
            findNavController().navigate(action)
        }
    }

    // ... (sisa kode)
    private fun observeFlaggedLokets() {
        viewLifecycleOwner.lifecycleScope.launch {
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
                            // Menggunakan differ dari HistoryAdapter
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
                    is Resource.Empty -> {
                        binding.shimmerLayout.stopShimmer()
                        binding.shimmerLayout.isVisible = false
                        binding.rvFlaggedLoket.isVisible = false
                        binding.tvEmpty.isVisible = true
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
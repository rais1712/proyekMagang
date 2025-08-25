// File: app/src/main/java/com/proyek/maganggsp/presentation/monitor/MonitorFragment.kt
package com.proyek.maganggsp.presentation.monitor

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import com.google.android.material.tabs.TabLayoutMediator
import com.proyek.maganggsp.databinding.FragmentMonitorBinding
import com.proyek.maganggsp.util.FeatureFlags
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MonitorFragment : Fragment() {

    private var _binding: FragmentMonitorBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val TAG = "MonitorFragment"
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMonitorBinding.inflate(inflater, container, false)

        // 🚩 FEATURE FLAGS: Check if monitor features should be enabled
        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.i(TAG, "🚩 MonitorFragment created")
            Log.d(TAG, "Monitor fragment enabled: ${FeatureFlags.ENABLE_MONITOR_FRAGMENT}")
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 🚩 FEATURE FLAGS: Conditional setup based on feature availability
        if (FeatureFlags.ENABLE_MONITOR_FRAGMENT) {
            setupViewPager()

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Monitor fragment disabled - showing placeholder")
            }
        }
    }

    private fun setupViewPager() {
        if (!FeatureFlags.ENABLE_MONITOR_FRAGMENT) {
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 ViewPager setup blocked by feature flag")
            }
            return
        }

        try {
            val pagerAdapter = MonitorPagerAdapter(this)
            binding.viewPager.adapter = pagerAdapter

            // Setup TabLayout with ViewPager2
            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                tab.text = when (position) {
                    0 -> "Dipantau"
                    1 -> "Diblokir"
                    else -> null
                }
            }.attach()

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 TabLayoutMediator attached successfully")
            }

        } catch (e: Exception) {
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.e(TAG, "🚩 Error setting up ViewPager", e)
            }
            showFeatureDisabledState()
        }
    }

    private fun showFeatureDisabledState() {
        // Hide ViewPager and TabLayout
        binding.viewPager.isVisible = false
        binding.tabLayout.isVisible = false

        // Show a placeholder message
        // Note: This assumes there's a TextView for showing messages in the layout
        // If not available, we can show a Toast instead
        try {
            // Try to find a TextView for messages (you might need to add this to layout)
            val messageView = binding.root.findViewById<android.widget.TextView>(
                com.proyek.maganggsp.R.id.tvMonitorMessage
            )

            if (messageView != null) {
                messageView.isVisible = true
                messageView.text = "Fitur monitoring sedang dikembangkan.\nSilakan gunakan fitur lain sementara waktu."
                messageView.textAlignment = View.TEXT_ALIGNMENT_CENTER
            } else {
                // Fallback to Toast if TextView not found
                showFeatureDisabledToast()
            }

        } catch (e: Exception) {
            // Fallback to Toast if anything goes wrong
            showFeatureDisabledToast()

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Could not find message TextView, using Toast", e)
            }
        }
    }

    private fun showFeatureDisabledToast() {
        Toast.makeText(
            requireContext(),
            "Fitur monitoring sedang dikembangkan",
            Toast.LENGTH_LONG
        ).show()
    }

    override fun onResume() {
        super.onResume()

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 MonitorFragment resumed")
        }

        // Show reminder if feature is disabled
        if (!FeatureFlags.ENABLE_MONITOR_FRAGMENT) {
            // Don't spam with toasts, just log
            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Monitor fragment feature disabled")
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
            Log.d(TAG, "🚩 MonitorFragment view destroyed")
        }

        _binding = null
    }
}
Log.d(TAG, "🚩 Monitor ViewPager setup completed")
}
} else {
    showFeatureDisabledState()

    if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
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
        } else {
            showFeatureDisabledState()
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
                    else -> "Tab $position"
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

        // 🚩 SURGICAL CUTTING: Create a simple disabled state message
        try {
            // Create a simple TextView programmatically if needed
            val messageView = android.widget.TextView(requireContext()).apply {
                text = """
                    🔧 Fitur Monitor sedang dikembangkan
                    
                    Fitur ini akan segera tersedia untuk memantau:
                    • Loket yang dipantau
                    • Loket yang diblokir
                    • Status real-time
                    
                    Silakan gunakan fitur lain sementara waktu.
                """.trimIndent()

                textAlignment = View.TEXT_ALIGNMENT_CENTER
                setPadding(32, 64, 32, 64)
                textSize = 16f
                setTextColor(resources.getColor(com.proyek.maganggsp.R.color.text_secondary_gray, null))
            }

            // Add to the root layout
            if (binding.root is android.view.ViewGroup) {
                (binding.root as android.view.ViewGroup).addView(
                    messageView,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT,
                    android.view.ViewGroup.LayoutParams.MATCH_PARENT
                )
            }

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.d(TAG, "🚩 Feature disabled state message shown")
            }

        } catch (e: Exception) {
            // Fallback to Toast if anything goes wrong
            showFeatureDisabledToast()

            if (FeatureFlags.ENABLE_DEBUG_LOGGING) {
                Log.w(TAG, "🚩 Could not create disabled state view, using Toast", e)
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

        // Show reminder if feature is disabled but only once
        if (!FeatureFlags.ENABLE_MONITOR_FRAGMENT && _binding != null) {
            // Don't spam with toasts, message is already shown in UI
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

    /**
     * 🚩 SURGICAL CUTTING: Public method to check if fragment is functional
     */
    fun isFeatureEnabled(): Boolean {
        return FeatureFlags.ENABLE_MONITOR_FRAGMENT
    }

    /**
     * 🚩 SURGICAL CUTTING: Get feature status message
     */
    fun getFeatureStatusMessage(): String {
        return if (FeatureFlags.ENABLE_MONITOR_FRAGMENT) {
            "Monitor fitur aktif"
        } else {
            "Monitor fitur sedang dikembangkan"
        }
    }
}